/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import java.util.*;
import java.util.Map.Entry;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.analysis.StaticAnalysis;
import uk.ac.ed.inf.biopepa.core.dom.*;

/**
 * Compiles a BioPEPA model
 * 
 * @author mtribast
 * 
 */
public class ModelCompiler implements DynamicExpressionModelContext {

	// listens to problems as collected by internal visitors
	IProblemRequestor problemRequestor;

	// The AST being compiled
	private Model model;

	// used to prevent cycles
	private String currentlyVisitedVariableName = null;

	// Model variables, as found by the compiler
	private Map<String, VariableData> variables = new HashMap<String, VariableData>();

	private Set<String> dynamicComponents = new HashSet<String>();

	// Model compartments
	private Map<String, CompartmentData> compartments = new HashMap<String, CompartmentData>();

	// Model species
	private Map<String, SpeciesData> species = new HashMap<String, SpeciesData>();

	// Definitions of functional rates
	private Map<String, FunctionalRateData> functionalRates = new HashMap<String, FunctionalRateData>();

	// Definitions of components
	private Map<String, ComponentData> components = new HashMap<String, ComponentData>();

	// Compositional definitions
	private Map<String, SystemEquationData> compositions = new HashMap<String, SystemEquationData>();

	private SystemEquationNode systemEquation = null;

	private List<ProblemInfo> problems = new ArrayList<ProblemInfo>();

	public ModelCompiler(Model model) {
		if (model == null)
			throw new IllegalArgumentException();
		this.model = model;
		this.problemRequestor = new IProblemRequestor() {

			IProblemPolicy policy = new DefaultProblemPolicy();

			public boolean accept(ProblemKind problem, ASTNode affectedNode) {
				return accept(problem, "", affectedNode);
			}

			public boolean accept(ProblemInfo.Severity severity, String message, ASTNode node) {
				ProblemInfo info = new ProblemInfo();
				info.severity = severity;
				info.message = message;
				info.sourceRange = node.getSourceRange();
				problems.add(info);
				return true;
			}

			public boolean accept(ProblemKind problem, String additionalComment, ASTNode affectedNode) {
				StringBuffer message = new StringBuffer(problem.toString());
				message.append(additionalComment).append(".");
				ProblemInfo info = new ProblemInfo();
				info.severity = (policy.isWarning(problem) ? ProblemInfo.Severity.WARNING : ProblemInfo.Severity.ERROR);
				info.message = message.toString();
				info.sourceRange = affectedNode.getSourceRange();
				problems.add(info);
				return true;
			}

			public IProblemPolicy getProblemPolicy() {
				return policy;
			}

			public void setProblemPolicy(IProblemPolicy policy) {
				throw new IllegalArgumentException();
			}

		};

	}

	public ProblemInfo[] compile() {
		try {
			discoverDynamicComponents();
			compileVariableDeclarations();
			compileCompartmentDeclarations();
			compileSpeciesDeclarations();
			compileFunctionalRates();
			compileComponents();
			compileSystemEquation();
			compileOutWildCards();
			generateWarnings();
			problems.addAll(StaticAnalysis.analysis(model, this));
		} catch (BioPEPAException e) {
		}
		return problems.toArray(new ProblemInfo[problems.size()]);
	}

	public boolean containsVariable(String name) {
		return variables.containsKey(name);
	}

	public boolean containsComponent(String name) {
		return components.containsKey(name);
	}

	public boolean containsCompartment(String name) {
		return compartments.containsKey(name);
	}

	public boolean containsSpecies(String name) {
		return species.containsKey(name);
	}

	public boolean containsFunctionalRate(String name) {
		return functionalRates.containsKey(name);
	}

	public boolean containsCompositionalDefinition(String name) {
		return compositions.containsKey(name);
	}

	public boolean containsAnyDeclaration(String name) {
		return containsCompartment(name) || containsVariable(name) || containsSpecies(name)
				|| containsFunctionalRate(name) || containsCompositionalDefinition(name) || containsComponent(name);
	}

	int getNumberOfDefinedCompartments() {
		return compartments.size();
	}

	String getCurrentlyVisitedVariable() {
		return currentlyVisitedVariableName;
	}

	public boolean isDynamic(String name) {
		return dynamicComponents.contains(name);
	}

	VariableData checkAndGetVariableData(String name) {
		VariableData data = variables.get(name);
		if (data == null)
			return null;
		data.registerNewUsage();
		return data;
	}

	CompartmentData checkAndGetCompartmentData(String name) {
		CompartmentData data = compartments.get(name);
		if (data == null)
			return null;
		data.registerNewUsage();
		return data;
	}

	SpeciesData checkAndGetSpeciesData(String identifier) {
		SpeciesData data = species.get(identifier);
		if (data == null)
			return null;
		data.registerNewUsage();
		return data;
	}

	FunctionalRateData checkAndGetFunctionalRate(String identifier) {
		FunctionalRateData data = functionalRates.get(identifier);
		if (data == null)
			return null;
		data.registerNewUsage();
		return data;

	}

	SystemEquationData checkAndGetComposition(String name) {
		SystemEquationData data = compositions.get(name);
		if (data == null)
			return null;
		data.registerNewUsage();
		return data;
	}

	ComponentData checkAndGetComponentData(String name) {
		ComponentData data = components.get(name);
		if (data == null)
			return null;
		data.registerNewUsage();
		return data;
	}

	/**
	 * TODO doesn't verify which are in use and which aren't; merely compiles a
	 * list of stated dynamic components.
	 * 
	 * @throws BioPEPAException
	 */
	private void discoverDynamicComponents() throws BioPEPAException {
		VariableDeclaration dec;
		Expression rhs;
		DynamicComponentGathererVisitor dcgv = new DynamicComponentGathererVisitor(this);
		Name name;
		for (Statement statement : model.statements()) {
			if (statement instanceof VariableDeclaration) {
				dec = (VariableDeclaration) statement;
				if (dec.getKind() == VariableDeclaration.Kind.CONTAINER) {
					name = dec.getName();
					if (name instanceof LocatedName)
						dynamicComponents.add(((LocatedName) name).getName());
					else
						dynamicComponents.add(dec.getName().getIdentifier());
				} else if (dec.getKind() == VariableDeclaration.Kind.COMPONENT) {
					rhs = dec.getRightHandSide();
					if (rhs instanceof Cooperation || rhs instanceof Component) {
						rhs.accept(dcgv);
					}
				}
			} else if (statement instanceof ExpressionStatement) {
				((ExpressionStatement) statement).getExpression().accept(dcgv);
			}
		}
		dynamicComponents.addAll(dcgv.components);
	}

	private void compileVariableDeclarations() throws BioPEPAException {
		// look for variable declarations and evaluate expressions
		for (Statement statement : model.statements()) {
			if (statement instanceof VariableDeclaration) {
				VariableDeclaration dec = (VariableDeclaration) statement;
				if (dec.getKind() == VariableDeclaration.Kind.VARIABLE) {
					VariableCompiler visitor = new VariableCompiler(this, dec);
					currentlyVisitedVariableName = dec.getName().getIdentifier();
					VariableData data = (VariableData) visitor.getData();
					assert data != null;
					variables.put(data.getName(), data);
				}
			}
		}
		currentlyVisitedVariableName = null;
	}

	private void compileCompartmentDeclarations() throws BioPEPAException {
		for (Statement statement : model.statements()) {
			if (statement instanceof VariableDeclaration) {
				VariableDeclaration dec = (VariableDeclaration) statement;
				if (dec.getKind() == VariableDeclaration.Kind.CONTAINER) {

					CompartmentCompiler visitor = new CompartmentCompiler(this, dec);
					CompartmentData data = (CompartmentData) visitor.getData();
					assert data != null;
					compartments.put(data.getName(), data);

				}
			}
		}

	}

	private void compileSpeciesDeclarations() throws BioPEPAException {
		List<SpeciesData> speciesList;
		for (Statement statement : model.statements()) {
			if (statement instanceof VariableDeclaration) {
				VariableDeclaration dec = (VariableDeclaration) statement;
				if (dec.getKind() == VariableDeclaration.Kind.SPECIES) {
					SpeciesDefinitionCompiler visitor = new SpeciesDefinitionCompiler(this, dec);
					speciesList = visitor.doGetDataList();
					for (SpeciesData sd : speciesList) {
						assert sd != null;
						species.put(sd.getName(), sd);
					}
				}
			}
		}
	}

	private void compileFunctionalRates() throws BioPEPAException {
		for (Statement statement : model.statements()) {
			if (statement instanceof VariableDeclaration) {
				VariableDeclaration dec = (VariableDeclaration) statement;
				if (dec.getKind() == VariableDeclaration.Kind.FUNCTION) {
					FunctionalRateCompiler visitor = new FunctionalRateCompiler(this,
							VariableDeclaration.Kind.FUNCTION, dec);
					FunctionalRateData data = (FunctionalRateData) visitor.getData();
					assert data != null;
					functionalRates.put(data.getName(), data);
				}
			}
		}
	}

	private void compileComponents() throws BioPEPAException {
		VariableDeclaration dec;
		Expression rhs;
		ComponentCompiler cc;
		ComponentData cd;
		for (Statement statement : model.statements())
			if (statement instanceof VariableDeclaration) {
				dec = (VariableDeclaration) statement;
				if (dec.getKind() == VariableDeclaration.Kind.COMPONENT) {
					rhs = dec.getRightHandSide();
					// looking for compositional definitions
					if (!(rhs instanceof Cooperation || rhs instanceof Component)) {
						cc = new ComponentCompiler(this, dec);
						cd = (ComponentData) cc.getData();
						assert cd != null;
						components.put(cd.getName(), cd);
					}
				}
			}
		// Handle compositional declarations last to allow for checking that a
		// component definition exists
		for (Statement statement : model.statements())
			if (statement instanceof VariableDeclaration) {
				dec = (VariableDeclaration) statement;
				if (dec.getKind() == VariableDeclaration.Kind.COMPONENT) {
					rhs = dec.getRightHandSide();
					if (rhs instanceof Cooperation || rhs instanceof Component) {
						if (containsAnyDeclaration(dec.getName().getIdentifier())) {
							problemRequestor.accept(ProblemKind.DUPLICATE_USAGE, dec);
							throw new CompilerException();
						}
						CompositionalVisitor visitor = new CompositionalVisitor(this);
						rhs.accept(visitor);
						String identifier = dec.getName().getIdentifier();
						SystemEquationData data = new SystemEquationData(identifier, dec);
						data.setSystemEquationNode(visitor.getData());
						compositions.put(identifier, data);
					}
				}
			}
	}

	private void compileSystemEquation() throws BioPEPAException {
		boolean foundSystemEquation = false;
		List<Statement> statements = model.statements();
		for (Statement s : statements) {
			if (s instanceof ExpressionStatement) {
				if (!foundSystemEquation) {
					Expression systemEquation = ((ExpressionStatement) s).getExpression();
					CompositionalVisitor v = new CompositionalVisitor(this);
					systemEquation.accept(v);
					this.systemEquation = v.result;
					foundSystemEquation = true;
				} else {
					problemRequestor.accept(ProblemKind.MULTIPLE_SYSTEM_EQUATIONS, s);
					throw new CompilerException();
				}
			}
		}
		if (!foundSystemEquation){
			problemRequestor.accept(ProblemKind.NO_SYSTEM_EQUATION, 
					statements.get(statements.size() - 1));
		}
	}

	private void compileOutWildCards() {
		ActionSetCompiler asc = new ActionSetCompiler(this);
		asc.computeWildCardSets();
	}

	private void generateWarnings() {
		findUnused(variables);
		findUnused(compartments);
		// removed because species data is optional
		// findUnused(species);
		findUnused(components);
		findUnused(functionalRates);
		findUnused(compositions);
	}

	public VariableData getVariableData(String name) {
		return variables.get(name);
	}
	
	public CompiledExpression getDynamicExpression(String name) {
		VariableData vData = this.getVariableData(name);
		if (vData != null){
			return vData.getValue();
		} else {
			return null;
		}
	}


	public CompartmentData getCompartmentData(String name) {
		return compartments.get(name);
	}

	public ComponentData getComponentData(String name) {
		return components.get(name);
	}

	public SpeciesData getSpeciesData(String identifier) {
		return species.get(identifier);
	}

	public FunctionalRateData getFunctionalRate(String identifier) {
		return functionalRates.get(identifier);
	}

	public SystemEquationData getComposition(String name) {
		return compositions.get(name);
	}

	public SystemEquationNode getSystemEquation() {
		return systemEquation;
	}

	private void findUnused(Map<String, ?> map) {
		for (Map.Entry<String, ?> entry : map.entrySet()) {
			Data datum = (Data) entry.getValue();
			if (datum.usages == 0) {
				this.problemRequestor.accept(ProblemInfo.Severity.INFO, ProblemKind.DEFINITION_DECLARED_BUT_NOT_USED
						.toString(), datum.declaration);
			}
		}
	}

	public Collection<ComponentData> getComponents(){
		return components.values();
	}
	
	public Map<String, FunctionalRateData> getFunctionalRateMap(){
		return functionalRates;
	}
	
	public Collection<VariableData> getAllVariables() {
		return variables.values();
	}

	public VariableData[] getDynamicVariables() {
		List<VariableData> list = new ArrayList<VariableData>();
		for (VariableData vd : variables.values())
			if (vd.isDynamic())
				list.add(vd);
		return list.toArray(new VariableData[] {});
	}

	public VariableData[] getStaticVariables() {
		List<VariableData> list = new ArrayList<VariableData>();
		for (VariableData vd : variables.values())
			if (!vd.isDynamic())
				list.add(vd);
		return list.toArray(new VariableData[] {});
	}

	void debug() {
		System.out.println("Variables:\n****");
		for (VariableData d : variables.values()) {
			System.out.println(d.name + "=" + d.getValue() + " (" + d.getUsage() + ")");
		}
		System.out.println("Compartments:\n****");
		for (CompartmentData d : compartments.values()) {
			System.out.println(d);
		}
		System.out.println("Species:\n****");
		for (SpeciesData d : species.values()) {
			System.out.println(d);
		}
		System.out.println("Functional Rates:\n****");
		for (FunctionalRateData d : functionalRates.values()) {
			System.out.println(d + "(" + d.getUsage() + ")");
		}

		System.out.println("Components:\n****");
		for (ComponentData d : components.values()) {
			System.out.println(d);
		}

		System.out.println("System Equation:\n***");
		System.out.println(systemEquation);
	}

	
}
