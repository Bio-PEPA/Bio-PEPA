/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.analysis;

import java.util.*;

import uk.ac.ed.inf.biopepa.core.compiler.*;

public class ActionCooperationAnalysis {

	private ModelCompiler compiledModel;

	private List<ProblemInfo> problems = new ArrayList<ProblemInfo>();

	private SystemEquationVisitor sev = new SystemEquationVisitor();

	ActionCooperationAnalysis() {
	}

	private class Actions {
		Set<String> actions = new HashSet<String>(), transportations = new HashSet<String>();
	}

	static List<ProblemInfo> checkActions(ModelCompiler compiledModel) {
		ActionCooperationAnalysis aca = new ActionCooperationAnalysis();
		aca.compiledModel = compiledModel;
		aca.sev.visit(compiledModel.getSystemEquation());
		return aca.problems;
	}

	private class SystemEquationVisitor {

		Map<ComponentData, Set<String>> found = new HashMap<ComponentData, Set<String>>();

		private Map<ComponentNode, Actions> visit(SystemEquationNode node) {
			if (node instanceof ComponentNode)
				return visit((ComponentNode) node);
			else if (node instanceof CooperationNode)
				return visit((CooperationNode) node);
			else
				throw new IllegalArgumentException("Unrecognised subclass of SystemEquationNode.");
		}

		private Map<ComponentNode, Actions> visit(CooperationNode node) {
			Map<ComponentNode, Actions> left = visit(node.getLeft());
			Map<ComponentNode, Actions> right = visit(node.getRight());
			Set<String> coopSet = new HashSet<String>();
			for (String s : node.getActions())
				coopSet.add(s);
			boolean wildcard = (coopSet.size() == 1 && coopSet.contains(("*")));
			Actions leftActions, rightActions;
			ComponentData cd;
			String s;
			Set<String> tSet;
			for (ComponentNode leftCN : left.keySet()) {
				leftActions = left.get(leftCN);
				for (ComponentNode rightCN : right.keySet()) {
					rightActions = right.get(rightCN);
					for (String transport : leftActions.transportations) {
						if (wildcard || coopSet.contains(transport)) {
							if (rightActions.transportations.contains(transport)
									&& !leftCN.getComponent().equals(rightCN.getComponent())) {
								// for(PrefixData pd :
								// compiledModel.getComponentData(leftCN.getComponent()).getPrefixes())
								// {
								// if(transport.equals(pd.getFunction()))
								cd = compiledModel.getComponentData(leftCN.getComponent());
								s = ProblemKind.INVALID_TRANSPORTATION_COOPERATION.getMessage() + " "
										+ leftCN.getName() + " and " + rightCN.getName() + " over " + transport;
								if (!found.containsKey(cd)) {
									tSet = new HashSet<String>();
									tSet.add(s);
									found.put(cd, tSet);
									problems.add(new ProblemInfo(s, cd));
								} else if (!found.get(cd).contains(s)) {
									found.get(cd).add(s);
									problems.add(new ProblemInfo(s, cd));
								}
								cd = compiledModel.getComponentData(rightCN.getComponent());
								s = ProblemKind.INVALID_TRANSPORTATION_COOPERATION.getMessage() + " "
										+ rightCN.getName() + " and " + leftCN.getName() + " over " + transport;
								if (!found.containsKey(cd)) {
									tSet = new HashSet<String>();
									tSet.add(s);
									found.put(cd, tSet);
									problems.add(new ProblemInfo(s, cd));
								} else if (!found.get(cd).contains(s)) {
									found.get(cd).add(s);
									problems.add(new ProblemInfo(s, cd));
								}
							} else if (rightActions.actions.contains(transport)) {
								cd = compiledModel.getComponentData(rightCN.getComponent());
								s = "Illegal cooperation by " + rightCN.getComponent() + " on " + transport
										+ " action. Cannot cooperate with transportation in species "
										+ leftCN.getComponent() + ".";
								if (!found.containsKey(cd)) {
									tSet = new HashSet<String>();
									tSet.add(s);
									found.put(cd, tSet);
									problems.add(new ProblemInfo(s, cd));
								} else if (!found.get(cd).contains(s)) {
									found.get(cd).add(s);
									problems.add(new ProblemInfo(s, cd));
								}
							}
						}
					}
					for (String transport : rightActions.transportations)
						if ((wildcard || coopSet.contains(transport)) && leftActions.actions.contains(transport)) {
							cd = compiledModel.getComponentData(leftCN.getComponent());
							s = "Illegal cooperation by " + leftCN.getComponent() + " on " + transport
									+ " action. Cannot cooperate with transportation in species "
									+ rightCN.getComponent() + ".";
							if (!found.containsKey(cd)) {
								tSet = new HashSet<String>();
								tSet.add(s);
								found.put(cd, tSet);
								problems.add(new ProblemInfo(s, cd));
							} else if (!found.get(cd).contains(s)) {
								found.get(cd).add(s);
								problems.add(new ProblemInfo(s, cd));
							}
						}
				}
			}
			left.putAll(right);
			return left;
		}

		private Map<ComponentNode, Actions> visit(ComponentNode node) {
			ComponentData cd = compiledModel.getComponentData(node.getComponent());
			Actions actions = new Actions();
			for (PrefixData pd : cd.getPrefixes()) {
				if (pd instanceof TransportData)
					actions.transportations.add(pd.getFunction());
				else
					actions.actions.add(pd.getFunction());
			}
			Map<ComponentNode, Actions> map = new HashMap<ComponentNode, Actions>();
			map.put(node, actions);
			return map;
		}
	}

}
