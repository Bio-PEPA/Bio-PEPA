/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import java.util.*;

import uk.ac.ed.inf.biopepa.core.dom.Cooperation;

class ActionSetCompiler {

	private ModelCompiler model;

	private Set<String> actionSet;

	private String[] sArray;

	ActionSetCompiler(ModelCompiler model) {
		this.model = model;
	}

	void computeWildCardSets() {
		traverse(model.getSystemEquation());
	}

	private void traverse(SystemEquationNode systemEquationNode) {
		if (systemEquationNode instanceof ComponentNode)
			traverse((ComponentNode) systemEquationNode);
		else if (systemEquationNode instanceof CooperationNode)
			traverse((CooperationNode) systemEquationNode);
	}

	private void traverse(ComponentNode componentNode) {
		ComponentData cd = model.getComponentData(componentNode.getComponent());
		actionSet = new HashSet<String>();
		for (PrefixData pd : cd.getPrefixes())
			if (pd instanceof ActionData) {
				List<String> locations = ((ActionData) pd).getLocations();
				if (locations.size() == 0) // always applicable
					actionSet.add(pd.function);
				else {
					// multiple locations means located components have already
					// been verified.
					String location = componentNode.getCompartment().getName();
					for (String s : locations)
						if (location.equals(s)) {
							actionSet.add(pd.function);
							break;
						}
				}
			} else if (pd instanceof TransportData) {
				// transportation
				TransportData td = (TransportData) pd;
				String location = componentNode.getCompartment().getName();
				if (location.equals(td.getSourceLocation()) || location.equals(td.getTargetLocation()))
					actionSet.add(pd.function);
			}

	}

	private void traverse(CooperationNode cooperationNode) {
		traverse(cooperationNode.getLeft());
		Set<String> leftSet = actionSet;
		traverse(cooperationNode.getRight());
		sArray = cooperationNode.getActions();
		if (sArray.length == 1 && sArray[0].equals(Cooperation.WILDCARD)) {
			ArrayList<String> al = new ArrayList<String>();
			for (String s : leftSet)
				if (actionSet.contains(s)) {
					al.add(s);
					// Ensures actions get the correct number of references when
					// using wildcards
					model.checkAndGetFunctionalRate(s);
				}
			cooperationNode.setActions(al.toArray(new String[] {}));
		}
		actionSet.addAll(leftSet);
	}
}
