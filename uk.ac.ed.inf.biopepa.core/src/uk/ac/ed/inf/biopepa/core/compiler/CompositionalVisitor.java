/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.Component;
import uk.ac.ed.inf.biopepa.core.dom.Cooperation;
import uk.ac.ed.inf.biopepa.core.dom.Expression;
import uk.ac.ed.inf.biopepa.core.dom.LocatedName;
import uk.ac.ed.inf.biopepa.core.dom.Name;
import uk.ac.ed.inf.biopepa.core.dom.PropertyLiteral.Kind;

;

/**
 * @author ajduguid
 * 
 */
public class CompositionalVisitor extends DefaultCompilerVisitor {

	protected SystemEquationNode result = null;

	private boolean locationsRequired = false;

	public CompositionalVisitor(ModelCompiler compiler) {
		super(compiler);
		locationsRequired = compiler.getNumberOfDefinedCompartments() > 0;
	}

	public SystemEquationNode getData() {
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.Name)
	 */
	@Override
	public boolean visit(Name name) throws BioPEPAException {
		SystemEquationData data = compiler.checkAndGetComposition(name.getIdentifier());
		if (data == null) {
			ProblemKind pKind = ProblemKind.VARIABLE_USED_BUT_NOT_DEFINED;
			pKind.setMessage("Variable: " + name.getIdentifier() + 
					         " used but not defined");
			compiler.problemRequestor.accept(pKind, name);
			throw new CompilerException();
		}
		this.result = data.getSystemEquationNode();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.Component)
	 */
	@Override
	public boolean visit(Component component) throws BioPEPAException {
		Expression level = component.getLevel();
		ExpressionEvaluatorVisitor v = new ExpressionEvaluatorVisitor(compiler);
		level.accept(v);
		CompiledExpression compiledExpression = v.getExpressionNode();
		ComponentNode nodeInfo = new ComponentNode(compiledExpression);
		Name name = component.getName();
		String identifier;
		if (name instanceof LocatedName) {
			identifier = ((LocatedName) name).getName();
			List<Name> names = ((LocatedName) name).getLocations().names();
			if (names.size() > 1) {
				compiler.problemRequestor.accept(ProblemKind.INVALID_NUMBER_OF_LOCATIONS, component);
				throw new CompilerException();
			}
			CompartmentData cd = compiler.checkAndGetCompartmentData(names.get(0).getIdentifier());
			if (cd == null) {
				compiler.problemRequestor.accept(ProblemKind.LOCATION_USED_BUT_NOT_DEFINED, component);
				throw new CompilerException();
			}
			nodeInfo.setCompartment(cd);
		} else {
			if (locationsRequired) {
				compiler.problemRequestor.accept(ProblemInfo.Severity.ERROR, 
						name.getIdentifier() + " does specify a location.",
						component);
				// throw new CompilerException();
			}
			identifier = component.getName().getIdentifier();
		}
		if (compiler.checkAndGetComponentData(identifier) == null) {
			compiler.problemRequestor.accept(ProblemKind.SPECIES_NOT_DECLARED, 
												component);
			throw new CompilerException();
		}
		nodeInfo.setComponent(identifier);
		
		if (!(compiledExpression instanceof CompiledNumber)) { // evaluates
																	// to a
																	// non-integer
			compiler.problemRequestor.accept(ProblemKind.DYNAMIC_VALUE, level);
			throw new CompilerException();
		}
		CompiledNumber enn = (CompiledNumber) compiledExpression;
		if (!enn.evaluatesToLong()) {
			compiler.problemRequestor.accept(ProblemKind.NON_INTEGER_VALUE, level);
			throw new CompilerException();
		}
		long count = enn.longValue();
		if (count < 0) {
			compiler.problemRequestor.accept(ProblemKind.INITIAL_COUNT_LT_ZERO, level);
			throw new CompilerException();
		}
		SpeciesData data = compiler.checkAndGetSpeciesData(identifier);
		if (data != null) {
			if (data.getMaximumConcentration() < count) {
				compiler.problemRequestor.accept(ProblemKind.INITIAL_COUNT_GT_MAX, level);
				throw new CompilerException();
			}
			if (data.isSetProperty(Kind.MIN) && data.getMinimalConcentration() > count) {
				compiler.problemRequestor.accept(ProblemKind.INITIAL_COUNT_LT_MIN, level);
				throw new CompilerException();
			}
		}
		nodeInfo.setCount(count);
		result = nodeInfo;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.Cooperation)
	 */
	@Override
	public boolean visit(Cooperation cooperation) throws BioPEPAException {
		CooperationNode result = new CooperationNode();
		CompositionalVisitor lhsVisitor = new CompositionalVisitor(this.compiler);
		cooperation.getLeftHandSide().accept(lhsVisitor);
		CompositionalVisitor rhsVisitor = new CompositionalVisitor(compiler);
		cooperation.getRightHandSide().accept(rhsVisitor);
		result.setLeft(lhsVisitor.getData());
		result.setRight(rhsVisitor.getData());

		ArrayList<String> actions = new ArrayList<String>();
		// Added to cover wildcard. All expressions are compiled before sets are
		// generated.
		List<Name> tActions = cooperation.getActionSet().names();
		if (tActions.size() == 1 && tActions.get(0).getIdentifier().equals(Cooperation.WILDCARD))
			actions.add(Cooperation.WILDCARD);
		else {
			String actionName;
			for (Name name : tActions) {
				actionName = name.getIdentifier();
				if (compiler.checkAndGetFunctionalRate(actionName) == null) {
					compiler.problemRequestor.accept(ProblemKind.FUNCTIONAL_RATE_USED_BUT_NOT_DECLARED, name);
					throw new CompilerException();
				}
				if (actions.contains(actionName)) {
					// This is just a warning
					compiler.problemRequestor.accept(ProblemKind.DUPLICATE_ACTION_FOUND, name);
				}
				// TODO See semantics of returned boolean
				actions.add(actionName);
			}
		}
		result.setActions(actions.toArray(new String[actions.size()]));
		this.result = result;
		return true;
	}

}
