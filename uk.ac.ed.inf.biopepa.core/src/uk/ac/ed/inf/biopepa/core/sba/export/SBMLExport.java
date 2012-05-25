/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.sba.export;

import java.util.*;
import java.util.regex.Pattern;

import uk.ac.ed.inf.biopepa.core.compiler.*;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledFunction.Function;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledOperatorNode.Operator;
import uk.ac.ed.inf.biopepa.core.interfaces.Exporter;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.SBAReaction;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour.Type;

/**
 * 
 * @author ajduguid
 * 
 */
public class SBMLExport implements Exporter {

	public static final String sbmlOpeningMathElement = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\">";
	public static final String sbmlClosingMathElement = "</math>";
	public static final String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	public static final String sbmlHeader = "<sbml xmlns=\"http://www.sbml.org/sbml/level2/version3\" level=\"2\" version=\"3\">";
	public static final String sbmlTimeSymbol = "<csymbol encoding=\"text\" definitionURL=\"http://www.sbml.org/sbml/symbols/time\"> t </csymbol>";
	private static String term = System.getProperty("line.separator");

	private SBAModel model;
	private String name = null;
	private boolean fMM, H;
	private static final String description;
	private StringBuilder sbml, parameters, initialAssignment, reactions, rules;
	private int parameterIndendation, assignmentIndentation, rulesIndentation;

	private Set<String> recordedVariables = new HashSet<String>();

	private Map<String, String> sbmlMap = new HashMap<String, String>();

	static {
		StringBuilder sb = new StringBuilder();
		sb.append("Systems Biology Markup Language Level 2 Version 3.").append(term);
		sb
				.append(
						"The Systems Biology Markup Language (SBML) is a computer-readable format for representing models of biological processes. It's applicable to simulations of metabolism, cell-signaling, and many other topics.")
				.append(term);
		sb.append(term).append(xmlHeader).append(term);
		sb.append(sbmlHeader).append(term);
		sb.append("  <model id=\"...\"").append(term);
		sb.append("    ...").append(term);
		sb.append("  </model>").append(term);
		sb.append("</sbml>");
		description = sb.toString();
	}

	public SBMLExport() {
	}

	public void setModel(SBAModel model) {
		if (model == null)
			throw new NullPointerException("SBA model must be non-null");
		if (this.model != null)
			throw new IllegalStateException("Model has already been set.");
		this.model = model;
	}

	public void setName(String modelName) {
		name = modelName;
	}

	public String toString() {
		if (model == null)
			throw new IllegalStateException("Model has not been set using setModel/1");
		sbml = new StringBuilder();
		parameters = new StringBuilder();
		initialAssignment = new StringBuilder();
		rules = new StringBuilder();
		reactions = new StringBuilder();
		sbmlMap.clear();
		recordedVariables.clear();
		// Flatten namespace
		Map<String, String> flattened = new HashMap<String, String>();
		Set<String> problemNames = new HashSet<String>();
		CompartmentData[] compartments = model.getCompartments();
		String fString, s;
		for (int i = 0; i < compartments.length; i++) {
			s = compartments[i].getName();
			fString = flattenName(s);
			if (flattened.containsKey(fString)) {
				problemNames.add(s);
			} else
				flattened.put(fString, s);
		}
		SBAReaction[] reactionsArray = model.getReactions();
		for (int i = 0; i < reactionsArray.length; i++) {
			s = reactionsArray[i].getName();
			fString = flattenName(s);
			if (flattened.containsKey(fString)) {
				problemNames.add(s);
			} else
				flattened.put(fString, s);
		}
		ComponentNode[] components = model.getComponents();
		for (int i = 0; i < components.length; i++) {
			s = components[i].getName();
			fString = flattenName(s);
			if (flattened.containsKey(fString)) {
				problemNames.add(s);
			} else
				flattened.put(fString, s);
		}
		// Partial fill of sbmlMap
		for (Map.Entry<String, String> me : flattened.entrySet())
			if (!me.getKey().equals(me.getValue()))
				sbmlMap.put(me.getValue(), me.getKey());
		// Problematic names
		mapNames(problemNames);
		// Header xml
		sbml.append(xmlHeader).append(term);
		sbml.append(sbmlHeader).append(term);
		sbml.append("  <model id=\"");
		if (name != null && !name.equals(""))
			sbml.append(flattenName(name));
		else
			sbml.append(flattenName(Integer.toString(model.hashCode())));
		sbml.append("\">").append(term);
		// Parameter indentation needs to be set before reactions are processed
		parameterIndendation = 3;
		assignmentIndentation = 3;
		rulesIndentation = 3;
		// Reactions
		reactions.append("    <listOfReactions>").append(term);
		for (SBAReaction rn : model.getReactions())
			reactions.append(toSBML(rn, 3));
		reactions.append("    </listOfReactions>").append(term);
		// Footer xml
		reactions.append("  </model>").append(term);
		reactions.append("</sbml>");
		if (fMM || H) {
			sbml.append("    <listOfFunctionDefinitions>").append(term);
			if (fMM)
				sbml.append(sbmlFMM(3));
			if (H)
				sbml.append(sbmlH(3));
			sbml.append("    </listOfFunctionDefinitions>").append(term);
		}
		// Compartments
		sbml.append("    <listOfCompartmentTypes>").append(term);
		for (CompartmentData.Type type : CompartmentData.Type.values())
			sbml.append("      ").append(toSBML(type)).append(term);
		sbml.append("    </listOfCompartmentTypes>").append(term);
		sbml.append("    <listOfCompartments>").append(term);
		if (compartments.length > 0) // is this correct?
			for (int i = 0; i < compartments.length; i++) {
				sbml.append("      ");
				sbml.append(toSBML(compartments[i]));
				sbml.append(term);
			}
		else
			sbml.append("      <compartment id=\"main\" size=\"1.0\"/>").append(term);
		sbml.append("    </listOfCompartments>").append(term);
		// Species
		sbml.append("    <listOfSpecies>").append(term);
		for (int i = 0; i < components.length; i++)
			sbml.append("      ").append(toSBML(components[i])).append(term);
		sbml.append("    </listOfSpecies>").append(term);
				
		// We'll output the initial assignments after the list of
		// parameters, I'm not sure if this is required by the spec
		// but might be. However before we output the parameters we
		// had better process the initial assignments so that the
		// parameters used are added to the list of parameters.
		// Right now we are adding an initial assignment for each
		// species count.
		for (ComponentNode comp : components){
			// initialAssignment.append(toSBMLInitialAssign(comp,3)).append(term);
			CompiledExpression initAmountExp = comp.getInitialAmountExpression();
			SBMLRateGenerator sbmlRG = 
				new SBMLRateGenerator(initAmountExp, 
										assignmentIndentation + 3);
			sbmlRG.generate();
			String initialAmount = sbmlRG.toString();
			addInitialAssignment(comp.getName(), initialAmount);
		}
		
		// Parameters
		if (parameters.length() > 0) {
			sbml.append("    <listOfParameters>").append(term);
			sbml.append(parameters);
			sbml.append("    </listOfParameters>").append(term);
		}
		if (initialAssignment.length() > 0) {
			sbml.append("    <listOfInitialAssignments>").append(term);
			sbml.append(initialAssignment);
			sbml.append("    </listOfInitialAssignments>").append(term);
		}
		if (rules.length() > 0) {
			sbml.append("    <listOfRules>").append(term);
			sbml.append(rules);
			sbml.append("    </listOfRules>").append(term);
		}
		sbml.append(reactions);
		return sbml.toString();
	}

	/*
	private String toSBMLInitialAssign (ComponentNode comp, int indentation){
		String indent = makeIndentation(indentation);
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<initialAssignment symbol=\"");
		String comp_name = comp.getName();
		if (sbmlMap.containsKey(comp_name)){
			comp_name = sbmlMap.get(comp_name);
		}
		sb.append(comp_name);
		sb.append("\">").append(term);
		sb.append(indent).append("  ").append("<math xmlns=\"http://www.w3.org/1998/Math/MathML\">").append(term);
		
		CompiledExpression initAmountExp = comp.getInitialAmountExpression();
		SBMLRateGenerator sbmlRG = 
			new SBMLRateGenerator(initAmountExp, 
									indentation + 3);
		sbmlRG.generate();
		String initialAmount = sbmlRG.toString();
		sb.append(indent).append("    ").append(initialAmount).append(term);
		
    	sb.append(indent).append("  ").append("</math>").append(term);
    	sb.append(indent).append("</initialAssignment>").append(term);
    	
    	return sb.toString();
	}
	*/
	
	private void mapNames(Set<String> names) {
		String s;
		Set<String> currentlyUsed = new HashSet<String>();
		currentlyUsed.addAll(sbmlMap.values());
		Pattern p;
		ArrayList<Integer> numbers;
		int[] intArray;
		for (String next : names) {
			s = flattenName(next);
			numbers = new ArrayList<Integer>();
			p = Pattern.compile(s + "_\\d+");
			for (String existing : currentlyUsed)
				if (p.matcher(existing).matches())
					numbers.add(new Integer(existing.substring(s.length() + 1)));
			if (numbers.size() > 0) {
				intArray = new int[numbers.size()];
				for (int i = 0; i < intArray.length; i++)
					intArray[i] = numbers.get(i);
				Arrays.sort(intArray);
				s = s + "_" + (intArray[intArray.length - 1] + 1);
			} else
				s = s + "_2";
			currentlyUsed.add(s);
			sbmlMap.put(next, s);
		}

	}

	public Object toDataStructure() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	private String makeIndentation(int indentation){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indentation; i++)
			sb.append("  ");
		String indent = sb.toString();
		return indent;
	}
	
	private String sbmlFMM(int indentation) {
		String indent = makeIndentation(indentation);
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<functionDefinition id=\"fMM\">").append(term);
		sb.append(indent).append("  ").append(sbmlOpeningMathElement).append(term);
		sb.append(indent).append("    ").append("<lambda>").append(term);
		sb.append(indent).append("      ").append("<bvar><ci> v_M </ci></bvar>").append(term);
		sb.append(indent).append("      ").append("<bvar><ci> K_M </ci></bvar>").append(term);
		sb.append(indent).append("      ").append("<bvar><ci> S </ci></bvar>").append(term);
		sb.append(indent).append("      ").append("<bvar><ci> E </ci></bvar>").append(term);
		sb.append(indent).append("      ").append("<apply>").append(term);
		sb.append(indent).append("        ").append("<divide/>").append(term);
		sb.append(indent).append("        ").append("<apply>").append(term);
		sb.append(indent).append("          ").append("<times/>").append(term);
		sb.append(indent).append("          ").append("<ci> v_M </ci>").append(term);
		sb.append(indent).append("          ").append("<ci> S </ci>").append(term);
		sb.append(indent).append("          ").append("<ci> E </ci>").append(term);
		sb.append(indent).append("        ").append("</apply>").append(term);
		sb.append(indent).append("        ").append("<apply>").append(term);
		sb.append(indent).append("          ").append("<plus/>").append(term);
		sb.append(indent).append("          ").append("<ci> K_M </ci>").append(term);
		sb.append(indent).append("          ").append("<ci> S </ci>").append(term);
		sb.append(indent).append("        ").append("</apply>").append(term);
		sb.append(indent).append("      ").append("</apply>").append(term);
		sb.append(indent).append("    ").append("</lambda>").append("").append(term);
		sb.append(indent).append("  ").append(sbmlClosingMathElement).append("").append(term);
		sb.append(indent).append("</functionDefinition>").append(term);
		return sb.toString();
	}

	private String sbmlH(int indentation) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indentation; i++)
			sb.append("  ");
		String indent = sb.toString();
		sb = new StringBuilder();
		sb.append(indent).append("<functionDefinition id=\"H\">").append(term);
		sb.append(indent).append("  ").append(sbmlOpeningMathElement).append(term);
		sb.append(indent).append("    ").append("<lambda>").append(term);
		sb.append(indent).append("      ").append("<bvar><ci> X </ci></bvar>").append(term);
		// sb.append(indent).append("      ").append("<apply>").append(term);
		sb.append(indent).append("        ").append("<piecewise>").append(term);
		sb.append(indent).append("          ").append("<piece>").append(term);
		sb.append(indent).append("            ").append("<cn> 1 </cn>").append(term);
		sb.append(indent).append("            ").append("<apply>").append(term);
		sb.append(indent).append("              ").append("<gt/>").append(term);
		sb.append(indent).append("              ").append("<ci> X </ci>").append(term);
		sb.append(indent).append("              ").append("<cn> 0 </cn>").append(term);
		sb.append(indent).append("            ").append("</apply>").append(term);
		sb.append(indent).append("          ").append("</piece>").append(term);
		sb.append(indent).append("          ").append("<otherwise>").append(term);
		sb.append(indent).append("            ").append("<cn> 0 </cn>").append(term);
		sb.append(indent).append("          ").append("</otherwise>").append(term);
		sb.append(indent).append("        ").append("</piecewise>").append(term);
		// sb.append(indent).append("      ").append("</apply>").append(term);
		sb.append(indent).append("    ").append("</lambda>").append("").append(term);
		sb.append(indent).append("  ").append(sbmlClosingMathElement).append("").append(term);
		sb.append(indent).append("</functionDefinition>").append(term);
		return sb.toString();
	}

	private String toSBML(CompartmentData.Type type) {
		return "<compartmentType id=\"" + type.toString() + "\"/>";
	}

	private String toSBML(CompartmentData data) {
		StringBuilder sb = new StringBuilder();
		sb.append("<compartment id=\"").append(data.getName()).append("\"");
		sb.append(" compartmentType=\"").append(data.getType().toString()).append("\"");
		if (data.getType().getDimensions() != 3)
			sb.append(" spatialDimensions=\"").append(data.getType().getDimensions()).append("\"");
		sb.append(" size=\"").append(data.getVolume()).append("\"");
		if (data.getParent() != null)
			sb.append(" outside=\"").append(data.getParent()).append("\"");
		sb.append("/>");
		return sb.toString();
	}

	private String toSBML(ComponentNode node) {
		StringBuilder sb = new StringBuilder();
		sb.append("<species id=\"");
		if (!sbmlMap.containsKey(node.getName()))
			sb.append(node.getName()).append("\"");
		else {
			sb.append(sbmlMap.get(node.getName())).append("\"");
			sb.append(" name=\"").append(node.getComponent()).append("\"");
		}
		sb.append(" compartment=\"").append(node.getCompartment() == null ? "main" : node.getCompartment().getName())
				.append("\"");
		// sb.append(" initialAmount=\"").append(node.getCount()).append("\"");
		sb.append(" substanceUnits=\"item\" hasOnlySubstanceUnits=\"true\"");
		sb.append("/>");
		return sb.toString();
	}

	private String toSBML(SBAReaction reaction, int indentation) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indentation; i++)
			sb.append("  ");
		String tabs = sb.toString();
		sb = new StringBuilder();
		sb.append(tabs).append("<reaction id=\"");
		String name = reaction.getName();
		if (sbmlMap.containsKey(name))
			sb.append(sbmlMap.get(name)).append("\" name=\"").append(name);
		else
			sb.append(name);
		sb.append("\" reversible=\"false\">").append(term);
		// Reactants
		List<SBAComponentBehaviour> list = reaction.getReactants();
		for (SBAComponentBehaviour outerCB : list)
			if (outerCB.getType().equals(Type.REACTANT)) {
				sb.append(tabs).append("  <listOfReactants>").append(term);
				for (SBAComponentBehaviour cb : list)
					if (cb.getType().equals(Type.REACTANT))
						sb.append(tabs).append("    ").append(toSBML(cb)).append(term);
				sb.append(tabs).append("  </listOfReactants>").append(term);
				break;
			}
		// Products
		list = reaction.getProducts();
		if (list.size() > 0) {
			sb.append(tabs).append("  <listOfProducts>").append(term);
			for (SBAComponentBehaviour cb : list)
				sb.append(tabs).append("    ").append(toSBML(cb)).append(term);
			sb.append(tabs).append("  </listOfProducts>").append(term);
		}
		// Modifiers
		list = reaction.getReactants();
		for (SBAComponentBehaviour outerCB : list)
			if (!outerCB.getType().equals(Type.REACTANT)) {
				sb.append(tabs).append("  <listOfModifiers>").append(term);
				for (SBAComponentBehaviour cb : list)
					if (!cb.getType().equals(Type.REACTANT))
						sb.append(tabs).append("    ").append(toSBML(cb)).append(term);
				sb.append(tabs).append("  </listOfModifiers>").append(term);
				break;
			}
		sb.append(tabs).append("  <kineticLaw>").append(term);
		sb.append(tabs).append("    ").append(SBMLExport.sbmlOpeningMathElement).append(term);
		SBMLRateGenerator sbmlRG = new SBMLRateGenerator(reaction, indentation + 3);
		sbmlRG.generate();
		sb.append(sbmlRG.toString());
		sb.append(tabs).append("    ").append(SBMLExport.sbmlClosingMathElement).append(term);
		sb.append(tabs).append("  </kineticLaw>").append(term);
		sb.append(tabs).append("</reaction>").append(term);
		return sb.toString();
	}

	private String toSBML(SBAComponentBehaviour component) {
		StringBuilder sb = new StringBuilder();
		Type type = component.getType();
		if (type.equals(Type.CATALYST) || type.equals(Type.INHIBITOR) ||
				type.equals(Type.MODIFIER)){
			sb.append("<modifierSpeciesReference");
		} else {
			sb.append("<speciesReference");
		}
		sb.append(" species=\"");
		String name = component.getName();
		if (sbmlMap.containsKey(name))
			sb.append(sbmlMap.get(name));
		else
			sb.append(name);
		sb.append("\"");
		if (component.getStoichiometry() != 1)
			sb.append(" stoichiometry=\"").append(component.getStoichiometry()).append("\"");
		sb.append("/>");
		return sb.toString();
	}

	private class SBMLRateGenerator extends CompiledExpressionVisitor {

		StringBuilder sb = new StringBuilder();

		int indentation = 0;

		Operator lastOperator;

		SBAReaction reaction;
		CompiledExpression compiledExpression;
		
		/*
		 * The preferred constructor giving the reaction which will
		 * allow the translation of rates such as fMA(r).
		 */
		SBMLRateGenerator(SBAReaction reaction, int indentation) {
			this(reaction.getRate(), indentation);
			this.reaction = reaction;
		}
		
		/* You can also *not* provide the reaction, this is useful for
		 * translating expressions such as the initial assignments.
		 * If you do this you must be careful that there are no
		 * rate law functions such as fMA in the expression given.
		 */
		public SBMLRateGenerator(CompiledExpression ce, int indentation) {
			this.indentation = indentation;
			this.compiledExpression = ce;
		}
		
		
		void generate() {
			compiledExpression.accept(this);
		}

		@Override
		public boolean visit(CompiledDynamicComponent component) {
			if (component.hasExpandedForm())
				return component.returnExpandedForm().accept(this);
			String name = component.getName();
			if (!recordedVariables.contains(name)) {
				recordedVariables.add(name);
				CompiledExpression ce = model.getStaticExpression(name);
				ParameterVisitor pv = new ParameterVisitor();
				if (ce != null && ce.accept(pv)) {
					addParameter(name, ce.toString(), false);
				} else {
					SBMLRateGenerator sbmlRG;
					if (ce == null) {
						ce = model.getDynamicExpression(name);
						if (ce != null) {
							addParameter(name, (pv.number != null ? pv.number.toString() : "1"), true);
							sbmlRG = new SBMLRateGenerator(ce, rulesIndentation + 2);
							sbmlRG.generate();
							addDynamicVariableAssignment(name, sbmlRG.toString());
						} // else is a species so do nothing
					} else {
						// add initialAssignment (static)
						sbmlRG = new SBMLRateGenerator(ce, assignmentIndentation + 2);
						// addParameter(name, (pv.number != null ? pv.number.toString() : "1"), false);
						addParameter(name, null, false);
						sbmlRG.generate();
						addInitialAssignment(name, sbmlRG.toString());
					}
				}
			}
			sb = new StringBuilder();
			indentation();
			sb.append("<ci> ");
			if (sbmlMap.containsKey(component.getName()))
				sb.append(sbmlMap.get(component.getName()));
			else
				sb.append(component.getName());
			sb.append(" </ci>").append(term);
			return true;
		}
		
		@Override
		public boolean visit(CompiledFunction function) {
			if (function.hasExpandedForm())
				return function.returnExpandedForm().accept(this);
			sb = new StringBuilder();
			Function f = function.getFunction();
			int numberReactants = 0;
			if (reaction != null){
				numberReactants = reaction.getReactants().size();
			}
			boolean apply = !(f.equals(Function.fMA) && numberReactants == 0);
			if (apply) {
				indentation();
				sb.append("<apply>").append(term);
				indentation++;
				indentation();
				switch (f) {
				case LOG:
					sb.append("<ln/>").append(term);
					break;
				case EXP:
					sb.append("<exp/>").append(term);
					break;
				case CEILING:
					sb.append("<ceiling/>").append(term);
					break;
				case FLOOR:
					sb.append("<floor/>").append(term);
					break;
				case TANH:
					sb.append("<tanh/>").append(term);
					break;
				case H:
					sb.append("<ci> H </ci>").append(term);
					H = true;
					break;
				case fMA:
					sb.append("<times/>").append(term);
					lastOperator = Operator.MULTIPLY;
					break;
				case fMM:
					sb.append("<ci> fMM </ci>").append(term);
					fMM = true;
					break;
				default:
					throw new IllegalArgumentException();
				}
			}
			StringBuilder fSB = new StringBuilder(sb.toString());
			for (CompiledExpression ce : function.getArguments()) {
				ce.accept(this);
				fSB.append(sb.toString());
			}
			sb = fSB;
			/*
			 * TODO: Okay it's probably wrong to just do nothing if
			 * reaction is null, but I'm not sure what to do in this case.
			 * This indicates someone has done something silly like A[fMA(3)].
			 */
			if (f.isRateLaw() && reaction != null) {
				String name;
				switch (f) {
				case fMA:
					for (SBAComponentBehaviour cb : reaction.getReactants()) {
						name = cb.getName();
						indentation();
						sb.append("<ci> ");
						if (sbmlMap.containsKey(name))
							sb.append(sbmlMap.get(name));
						else
							sb.append(name);
						sb.append(" </ci>").append(term);
					}
					break;
				case fMM:
					SBAComponentBehaviour[] cbArray = new SBAComponentBehaviour[2];
					for (SBAComponentBehaviour cb : reaction.getReactants())
						if (cb.getType().equals(Type.REACTANT))
							cbArray[0] = cb;
						else
							cbArray[1] = cb;
					for (SBAComponentBehaviour cb : cbArray) {
						name = cb.getName();
						indentation();
						sb.append("<ci> ");
						if (sbmlMap.containsKey(name))
							sb.append(sbmlMap.get(name));
						else
							sb.append(name);
						sb.append(" </ci>").append(term);
					}
					break;
				default:
					throw new IllegalArgumentException();
				}
			}
			if (apply) {
				indentation--;
				indentation();
				sb.append("</apply>").append(term);
			}
			// sb = fSB;
			return true;
		}

		@Override
		public boolean visit(CompiledNumber number) {
			if (number.hasExpandedForm())
				return number.returnExpandedForm().accept(this);
			sb = new StringBuilder();
			indentation();
			sb.append("<cn> ");
			sb.append(number.evaluatesToLong() ? number.longValue() : number.doubleValue());
			sb.append(" </cn>").append(term);
			return true;
		}

		@Override
		public boolean visit(CompiledOperatorNode operator) {
			if (operator.hasExpandedForm())
				return operator.returnExpandedForm().accept(this);
			sb = new StringBuilder();
			Operator previousOp = lastOperator;
			lastOperator = operator.getOperator();
			boolean apply = !(lastOperator.equals(previousOp) && (previousOp.equals(Operator.MULTIPLY) || previousOp
					.equals(Operator.PLUS)));
			// times and plus can take multiple arguments.
			if (apply) {
				indentation();
				sb.append("<apply>").append(term);
				indentation++;
				indentation();
				switch (lastOperator) {
				case PLUS:
					sb.append("<plus/>").append(term);
					break;
				case MINUS:
					sb.append("<minus/>").append(term);
					break;
				case MULTIPLY:
					sb.append("<times/>").append(term);
					break;
				case DIVIDE:
					sb.append("<divide/>").append(term);
					break;
				case POWER:
					sb.append("<power/>").append(term);
					break;
				default:
					throw new IllegalArgumentException();
				}
			}
			StringBuilder opSB = new StringBuilder(sb.toString());
			operator.getLeft().accept(this);
			opSB.append(sb.toString());
			operator.getRight().accept(this);
			opSB.append(sb.toString());
			sb = opSB;
			if (apply) {
				indentation--;
				indentation();
				sb.append("</apply>").append(term);
			}
			lastOperator = previousOp;
			return true;
		}

		public boolean visit(CompiledSystemVariable variable) {
			if (variable.hasExpandedForm())
				return variable.returnExpandedForm().accept(this);
			sb = new StringBuilder();
			indentation();
			switch (variable.getVariable()) {
			case TIME:
				sb.append(sbmlTimeSymbol).append(term);
				break;
			default:
				throw new IllegalArgumentException();
			}
			return true;
		}

		private final void indentation() {
			for (int i = 0; i < indentation; i++)
				sb.append("  ");
		}

		public String toString() {
			return sb.toString();
		}
	}

	private void addParameter(String name, String value, boolean dynamic) {
		String sName;
		if (!name.equals(flattenName(name))) {
			Set<String> set = new HashSet<String>();
			set.add(name);
			mapNames(set);
			sName = sbmlMap.get(name);
		} else
			sName = name;
		for (int i = 0; i < parameterIndendation; i++)
			parameters.append("  ");
		parameters.append("<parameter id=\"").append(sName).append("\"");
		if (value != null){
			parameters.append(" value=\"").append(value).append("\"");
		}
		if (dynamic)
			parameters.append(" constant=\"false\"");
		parameters.append("/>").append(term);
	}

	private void addInitialAssignment(String name, String expression) {
		String sName;
		if (!name.equals(flattenName(name))) {
			Set<String> set = new HashSet<String>();
			set.add(name);
			mapNames(set);
			sName = sbmlMap.get(name);
		} else
			sName = name;
		StringBuilder indent = new StringBuilder();
		for (int i = 0; i < assignmentIndentation; i++)
			indent.append("  ");
		initialAssignment.append(indent).append("<initialAssignment symbol=\"");
		initialAssignment.append(sName).append("\">").append(term);
		initialAssignment.append(indent).append("  ").append(sbmlOpeningMathElement).append(term);
		initialAssignment.append(expression);
		initialAssignment.append(indent).append("  ").append(sbmlClosingMathElement).append(term);
		initialAssignment.append(indent).append("</initialAssignment>").append(term);
	}

	private void addDynamicVariableAssignment(String name, String expression) {
		String sName;
		if (!name.equals(flattenName(name))) {
			Set<String> set = new HashSet<String>();
			set.add(name);
			mapNames(set);
			sName = sbmlMap.get(name);
		} else
			sName = name;
		StringBuilder indent = new StringBuilder();
		for (int i = 0; i < rulesIndentation; i++)
			indent.append("  ");
		rules.append(indent).append("<assignmentRule variable=\"");
		rules.append(sName).append("\">").append(term);
		rules.append(indent).append("  ").append(sbmlOpeningMathElement).append(term);
		rules.append(expression);
		rules.append(indent).append("  ").append(sbmlClosingMathElement).append(term);
		rules.append(indent).append("</assignmentRule>").append(term);
	}

	public static String flattenName(String name) {
		char[] charArray = name.toCharArray();
		boolean prepend = !(Character.isLetter(charArray[0]) || charArray[0] == '_');
		for (int i = 1; i < charArray.length; i++)
			if (!Character.isLetterOrDigit(charArray[i]))
				charArray[i] = '_';
		return (prepend ? "_" : "") + new String(charArray);
	}

	public String getExportPrefix() {
		return "xml";
	}

	public void setModel(ModelCompiler compiledModel) {
		throw new UnsupportedOperationException();
	}

	public Object requiredDataStructure() {
		return SBAModel.class;
	}

	public String canExport() {
		if (model == null)
			throw new IllegalStateException("Model has not been set using setModel/1");
		SBMLParseable visitor = new SBMLParseable();
		for (SBAReaction sbar : model.getReactions()) {
			if (!sbar.getRate().accept(visitor))
				return visitor.response;
		}
		return null;
	}

	private class SBMLParseable extends CompiledExpressionVisitor {

		String response = null;

		@Override
		public boolean visit(CompiledDynamicComponent component) {
			return true;
		}

		@Override
		public boolean visit(CompiledFunction function) {
			switch (function.getFunction()) {
			case CEILING:
			case FLOOR:
			case EXP:
			case fMA:
			case fMM:
			case H:
			case LOG:
			case TANH:
				break;
			default:
				response = "Model uses the " + function.getFunction().toString()
						+ " function, which is currently not supported in exporting to SBML.";
				return false;
			}
			for (CompiledExpression ce : function.getArguments())
				if (!ce.accept(this))
					return false; // return false without further checking
			return true;
		}

		@Override
		public boolean visit(CompiledNumber number) {
			return true;
		}

		@Override
		public boolean visit(CompiledOperatorNode operator) {
			return operator.getLeft().accept(this) && operator.getRight().accept(this);
		}

		public boolean visit(CompiledSystemVariable variable) {
			switch (variable.getVariable()) {
			case TIME:
				return true;
			default:
				response = "Model uses the " + variable.toString()
						+ " variable, which is currently not supported in exporting to SBML.";
				return false;
			}
		}

	}

	public String getDescription() {
		return description;
	}

	public String getLongName() {
		return "Systems Biology Markup Language";
	}

	private class ParameterVisitor extends CompiledExpressionVisitor {

		Number number = null;

		@Override
		public boolean visit(CompiledDynamicComponent component) {
			return false;
		}

		@Override
		public boolean visit(CompiledFunction function) {
			return false;
		}
		

		@Override
		public boolean visit(CompiledNumber number) {
			this.number = number.getNumber();
			return !number.hasExpandedForm();
		}

		@Override
		public boolean visit(CompiledOperatorNode operator) {
			return false;
		}

		@Override
		public boolean visit(CompiledSystemVariable variable) {
			return false;
		}
	}

	public String getShortName() {
		return "SBML";
	}
}
