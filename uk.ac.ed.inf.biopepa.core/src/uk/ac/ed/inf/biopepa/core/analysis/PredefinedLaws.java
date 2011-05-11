/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.analysis;

import java.util.*;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.compiler.*;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledFunction.Function;
import uk.ac.ed.inf.biopepa.core.dom.VariableDeclaration.Kind;
import uk.ac.ed.inf.biopepa.core.dom.*;

public class PredefinedLaws {

	private static Map<String, FunctionCall> functionMap = new HashMap<String, FunctionCall>();

	private static List<ProblemInfo> problems = new ArrayList<ProblemInfo>();

	private static ExpressionVisitor expressionVisitor = new ExpressionVisitor();

	private static EquationVisitor equationVisitor = new EquationVisitor();

	static List<ProblemInfo> checkPredefinedLaws(Model model, ModelCompiler compiledModel) {
		VariableDeclaration vd;
		ExpressionStatement systemEquation = null;
		functionMap.clear();
		problems.clear();
		for (Statement statement : model.statements()) {
			if (statement instanceof VariableDeclaration) {
				vd = (VariableDeclaration) statement;
				if (vd.getKind().equals(Kind.FUNCTION)) {
					try {
						expressionVisitor.scan(vd.getRightHandSide());
					} catch (BioPEPAException e) {
						problems.add(new ProblemInfo(e.getMessage(), vd.getRightHandSide().getSourceRange()));
						continue;
					}
					if (expressionVisitor.functionsFound.size() > 1) {
						Function f;
						int i = 0;
						for (FunctionCall fc : expressionVisitor.functionsFound) {
							f = CompiledFunction.getFunction(fc.getName().getIdentifier());
							if (f.isRateLaw())
								i++;
						}
						if (i > 1)
							problems.add(new ProblemInfo("Embedded function call", vd.getRightHandSide()
									.getSourceRange()));
					} else if (expressionVisitor.functionsFound.size() == 1)
						functionMap.put(vd.getName().getIdentifier(), expressionVisitor.functionsFound.get(0));
				}
			} else if (statement instanceof ExpressionStatement)
				systemEquation = (ExpressionStatement) statement;
		}
		equationVisitor.model = compiledModel;
		try {
			systemEquation.accept(equationVisitor);
		} catch (BioPEPAException e) {
			// TODO
			problems.add(new ProblemInfo(e.getMessage(), systemEquation.getSourceRange()));
			return problems;
		}
		for (Map.Entry<String, FunctionCall> me : functionMap.entrySet()) {
			String s;
			if (equationVisitor.map.containsKey(me.getKey())) {
				FunctionCall fc = me.getValue();
				s = fc.getName().getIdentifier();
				Function f = CompiledFunction.getFunction(fc.getName().getIdentifier());
				for (int[] iArray : equationVisitor.map.get(me.getKey()))
					if (f.isRateLaw() && !match(s, iArray)) {
						problems
								.add(new ProblemInfo(
										"Predefined function does not have the correct number of components with required behaviours",
										me.getValue().getSourceRange()));
						break;
					}
			}
		}
		return problems;
	}

	private static class ExpressionVisitor extends DoNothingVisitor {

		List<FunctionCall> functionsFound = new ArrayList<FunctionCall>();

		public void scan(Expression expression) throws BioPEPAException {
			functionsFound.clear();
			expression.accept(this);
		}

		public boolean visit(Cooperation cooperation) throws BioPEPAException {
			throw new BioPEPAException("Unexpected cooperation");
		}

		public boolean visit(FunctionCall functionCall) throws BioPEPAException {
			functionsFound.add(functionCall);
			for (Expression e : functionCall.arguments())
				e.accept(this);
			return true;
		}

		public boolean visit(InfixExpression infixExpression) throws BioPEPAException {
			infixExpression.getLeftHandSide().accept(this);
			infixExpression.getRightHandSide().accept(this);
			return true;
		}

		public boolean visit(Prefix prefix) throws BioPEPAException {
			throw new BioPEPAException("Unexpected prefix");
		}

		public boolean visit(PropertyInitialiser propertyInitialiser) throws BioPEPAException {
			throw new BioPEPAException("Unexpected property initialiser");
		}

		public boolean visit(PropertyLiteral propertyLiteral) throws BioPEPAException {
			throw new BioPEPAException("Unexpected property literal");
		}
	}

	private static class EquationVisitor extends UnsupportedVisitor {

		Map<String, List<int[]>> map;

		ModelCompiler model;

		String s;

		public boolean visit(ExpressionStatement statement) throws BioPEPAException {
			statement.getExpression().accept(this);
			return true;
		}

		public boolean visit(Component component) throws BioPEPAException {
			map = new HashMap<String, List<int[]>>();
			int[] i;
			String sn, ln = null;
			List<String> locations;
			Name n = component.getName();
			boolean source = false;
			if (n instanceof LocatedName) {
				sn = ((LocatedName) n).getName();
				/*
				 * We can do the following because we know the model has
				 * compiled and each component in the model component has one
				 * location.
				 */
				ln = ((LocatedName) n).getLocations().names().get(0).getIdentifier();
			} else
				sn = n.getIdentifier();
			for (PrefixData pd : model.getComponentData(sn).getPrefixes()) {
				if (ln != null) {
					/*
					 * Again, because of checks already done we know every
					 * species in the model component will have a location if
					 * locations are used. We know this as these static checks
					 * are the last to be performed.
					 */
					if (pd instanceof ActionData) {
						locations = ((ActionData) pd).getLocations();
						if (locations.size() > 0 && !locations.contains(ln))
							continue;
					} else if (pd instanceof TransportData) {
						if (((TransportData) pd).getSourceLocation().equals(ln))
							source = true;
						else if (((TransportData) pd).getTargetLocation().equals(ln))
							source = false;
						else
							continue;
					}
				}
				s = pd.getFunction();
				i = new int[] { 0, 0, 0, 0, 0 };
				switch (pd.getOperator()) {
				case REACTANT:
					i[0] = 1;
					break;
				case ACTIVATOR:
					i[1] = 1;
					break;
				case INHIBITOR:
					i[2] = 1;
					break;
				case GENERIC:
					i[3] = 1;
					break;
				case PRODUCT:
					i[4] = 1;
					break;
				case UNI_TRANSPORTATION:
					i[0] = (source ? 1 : 0);
					i[4] = (source ? 0 : 1);
				case BI_TRANSPORTATION:
					i[0] = 1;
					i[4] = 1;
					break;
				default:
					throw new BioPEPAException("Unsupported Bio-PEPA operator.");
				}
				if (!map.containsKey(s))
					map.put(s, new ArrayList<int[]>());
				map.get(s).add(i);
			}
			return true;
		}

		public boolean visit(Cooperation cooperation) throws BioPEPAException {
			List<Name> tActions = cooperation.getActionSet().names();
			cooperation.getLeftHandSide().accept(this);
			Map<String, List<int[]>> left = map;
			cooperation.getRightHandSide().accept(this);
			// Wildcard sets aren't available from Model so additional code is
			// required.
			List<int[]> l;
			if (tActions.size() == 1 && tActions.get(0).getIdentifier().equals(Cooperation.WILDCARD)) {
				for (Map.Entry<String, List<int[]>> me : left.entrySet()) {
					s = me.getKey();
					if (map.containsKey(s)) {
						l = new ArrayList<int[]>();
						for (int[] i1 : me.getValue())
							for (int[] i2 : map.get(s))
								l.add(merge(i1, i2));
						map.put(s, l);
					} else
						map.put(s, me.getValue());
				}
				return true;
			}
			// else standard cooperation set
			for (Expression expression : tActions) {
				s = ((Name) expression).getIdentifier();
				if (left.containsKey(s) && map.containsKey(s)) {
					// merge into map
					l = new ArrayList<int[]>();
					for (int[] i1 : left.remove(s))
						for (int[] i2 : map.get(s))
							l.add(merge(i1, i2));
					map.put(s, l);
				}
			}
			for (Map.Entry<String, List<int[]>> me : left.entrySet()) {
				if (map.containsKey(me.getKey())) {
					map.get(me.getKey()).addAll(me.getValue());
					// problems.add(new ProblemInfo(ProblemKind.SYNTAX_ERROR,
					// "Non cooperation over shared action name",
					// cooperation.getActionSet().getSourceRange()));
				} else
					map.put(me.getKey(), me.getValue());
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * uk.ac.ed.inf.biopepa.core.dom.UnsupportedVisitor#visit(uk.ac.ed.inf
		 * .biopepa.core.dom.Name)
		 */
		public boolean visit(Name name) throws BioPEPAException {
			name.getBinding().getVariableDeclaration().getRightHandSide().accept(this);
			return true;
		}
	}

	private static int[] merge(int[] one, int[] two) {
		int[] iArray = new int[one.length];
		for (int i = 0; i < one.length; i++)
			iArray[i] = one[i] + two[i];
		return iArray;
	}

	private static boolean match(String fName, int[] count) {
		if (AST.Literals.MASS_ACTION.toString().equals(fName))
			return count[0] + count[4] > 0 && count[2] == 0;
		if (AST.Literals.MICHAELIS_MENTEN.toString().equals(fName))
			return count[0] == 1 && count[1] == 1 && count[2] == 0 && count[3] == 0 && count[4] == 1;
		if (AST.Literals.COMPETITIVE_INHIBITION.toString().equals(fName))
			return count[0] == 1 && count[1] == 1 && count[2] == 1 && count[3] == 0 && count[4] == 1;
		return false;
	}

}
