/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.sba;

import java.util.*;
import java.util.regex.Pattern;

import uk.ac.ed.inf.biopepa.core.compiler.*;

public class SBAModel implements DynamicExpressionModelContext {

	private class SystemEquationVisitor {

		Set<String> actions = new HashSet<String>();
		Map<String, List<SBAReaction>> currentReactions = new HashMap<String, List<SBAReaction>>();
		List<SBAReaction> newList;
		FunctionChecker fc = new FunctionChecker();

		void visit(ComponentNode node) {
			// componentCount++;
			SBAReaction reaction = null;
			SBAComponentBehaviour behaviour;
			String name = node.getComponent();
			String compartmentName = null;
			CompartmentData compartment = node.getCompartment();
			if (compartment != null) {
				compartmentName = compartment.getName();
				compartments.put(compartmentName, compartment.getVolume());
				if (components.containsKey(name))
					components.get(name).add(node);
				else {
					Set<ComponentNode> s = new HashSet<ComponentNode>();
					s.add(node);
					components.put(name, s);
				}
			} else {
				Set<ComponentNode> s = new HashSet<ComponentNode>();
				s.add(node);
				components.put(name, s);
			}
			CompiledExpression ce;
			ActionData ad;
			TransportData td;
			String reactionName;
			for (PrefixData prefix : compiledBioPEPA.getComponentData(node.getComponent()).getPrefixes()) {
				reactionName = prefix.getFunction();
				ce = compiledBioPEPA.getFunctionalRate(reactionName).getRightHandSide();
				ce.accept(fc);
				reaction = new SBAReaction(reactionName, ce);
				if (prefix instanceof ActionData) {
					// ActionData objects represent an atomic action, unlike
					// TransportData which will be broken down into two distinct reactions
					ad = (ActionData) prefix;
					if (ad.getLocations().size() > 0 && 
							!ad.getLocations().contains(compartment.getName())){
						continue;
					}
					behaviour = new SBAComponentBehaviour(name, compartmentName, 
															prefix.getOperator());
					behaviour.setStoichiometry((int) prefix.getStoichometry());
					
					reaction.addComponent(behaviour);
					recordReaction(reaction);
					
				} else if (prefix instanceof TransportData) {
					
					
					td = (TransportData) prefix;
					if (compartmentName.equals(td.getSourceLocation())) {
						behaviour = new SBAComponentBehaviour(name, compartmentName,
								SBAComponentBehaviour.Type.REACTANT);
						behaviour.setStoichiometry((int) prefix.getStoichometry());
					} else if (compartmentName.equals(td.getTargetLocation())) {
						behaviour = new SBAComponentBehaviour(name, td.getTargetLocation(),
								SBAComponentBehaviour.Type.PRODUCT);
						behaviour.setStoichiometry((int) prefix.getStoichometry());
					} else {
						continue;
					}
					
					if (td.getOperator().equals(PrefixData.Operator.BI_TRANSPORTATION)){
						reaction.reversible = true;
					}
					reaction.transportation = td;
					
					reaction.addComponent(behaviour);
					recordReaction(reaction);
					
					/*
					 * Working version that creates uni-directional copies based
					 * on source location
					 *
					td = (TransportData) prefix;
					if(!compartmentName.equals(td.getSourceLocation())){
						continue; 
					}
					behaviour = new SBAComponentBehaviour(name, 
							                              compartmentName,
							                              SBAComponentBehaviour.Type.REACTANT);
					reaction.addComponent(behaviour);
					behaviour = new SBAComponentBehaviour(name, 
							                              td.getTargetLocation(),
					                                      SBAComponentBehaviour.Type.PRODUCT);
					reaction.addComponent(behaviour);
					recordReaction(reaction);
					
					if(td.getOperator().equals(PrefixData.Operator.BI_TRANSPORTATION)) { 
						reaction = new SBAReaction(reactionName, ce);
						behaviour = new	SBAComponentBehaviour(name, 
								                              td.getTargetLocation(),
											                  SBAComponentBehaviour.Type.REACTANT);
					    reaction.addComponent(behaviour);
					    behaviour = new SBAComponentBehaviour(name,
						      	                              compartmentName,
					                                          SBAComponentBehaviour.Type.PRODUCT);
					    reaction.addComponent(behaviour);
					    recordReaction(reaction); 
					}
					*/
					
				} else {
					throw new IllegalArgumentException("Unrecognised subclass of PrefixData.");
				}
				reaction.addComponent(behaviour);
				recordReaction(reaction);
			}
		}

		private final void recordReaction(SBAReaction reaction) {
			if (currentReactions.containsKey(reaction.name))
				currentReactions.get(reaction.name).add(reaction);
			else {
				newList = new LinkedList<SBAReaction>();
				newList.add(reaction);
				currentReactions.put(reaction.name, newList);
			}
		}

		void visit(CooperationNode node) {
			visit(node.getLeft());
			Map<String, List<SBAReaction>> left = currentReactions;
			currentReactions = new HashMap<String, List<SBAReaction>>();
			visit(node.getRight());
			actions.clear();
			for (String s : node.getActions())
				actions.add(s);
			String action;
			for (Map.Entry<String, List<SBAReaction>> me : currentReactions.entrySet()) {
				action = me.getKey();
				if (actions.contains(action)) {
					// Synchronized action
					newList = new LinkedList<SBAReaction>();
                                        /*
					if (left == null){
						System.out.println("left is indeed null\n");
					}
					if (action == null){
						System.out.println("action is null\n");
					}
                                        */
					List<SBAReaction>leftActions = left.get(action);
					if (leftActions != null){
					  for (SBAReaction one : left.get(action)){
						  for (SBAReaction two : me.getValue()){
							newList.addAll(SBAReaction.merge(one, two));
						  }
					  }
					}
					left.put(action, newList);

				} else {
					// Parallel action
					if (left.containsKey(action))
						left.get(action).addAll(me.getValue());
					else {
						left.put(action, me.getValue());
					}
				}
			}
			currentReactions = left;
		}

		void visit(SystemEquationNode node) {
			if (node instanceof ComponentNode)
				visit((ComponentNode) node);
			else if (node instanceof CooperationNode)
				visit((CooperationNode) node);
			else
				throw new IllegalArgumentException("Unrecognised subclass of SystemEquationNode.");
		}

		private class FunctionChecker extends CompiledExpressionVisitor {

			@Override
			public boolean visit(CompiledDynamicComponent component) {
				return false;
			}

			@Override
			public boolean visit(CompiledFunction function) {
				switch (function.getFunction()) {
				case CEILING:
				case FLOOR:
				case H:
					nonDifferentiableFunctions = true;
					break;
				default:
				}
				for (CompiledExpression ce : function.getArguments())
					ce.accept(this);
				return true;
			}
			
			@Override
			public boolean visit(CompiledNumber number) {
				return true;
			}

			@Override
			public boolean visit(CompiledOperatorNode operator) {
				operator.getLeft().accept(this);
				operator.getRight().accept(this);
				return true;
			}

			@Override
			public boolean visit(CompiledSystemVariable variable) {
				switch (variable.getVariable()) {
				case TIME:
					timeDependentRates = true;
					break;
				default:
				}
				return true;
			}

		}
	}

	boolean timeDependentRates, nonDifferentiableFunctions;
	Map<String, Double> compartments = new HashMap<String, Double>();
	ModelCompiler compiledBioPEPA;
	// private int componentCount;
	private Map<String, Set<ComponentNode>> components = new HashMap<String, Set<ComponentNode>>();

	private Map<String, CompiledExpression> dynamicVariables = new HashMap<String, CompiledExpression>();

	private Set<String> dontInline = new HashSet<String>();

	private Map<String, SBAReaction> reactions = new HashMap<String, SBAReaction>();

	public SBAModel(ModelCompiler compiledBioPEPA) {
		this.compiledBioPEPA = compiledBioPEPA;
	}

	public CompartmentData[] getCompartments() {
		CompartmentData[] cd = new CompartmentData[compartments.size()];
		int i = 0;
		for (String s : compartments.keySet())
			cd[i++] = compiledBioPEPA.getCompartmentData(s);
		return cd;
	}

	/**
	 * 
	 * @return
	 */
	public int getComponentCount() {
		return components.size();
	}

	public String[] getComponentNames() {
		// String[] sArray = new String[componentCount];
		LinkedList<String> sList = new LinkedList<String>();
		// int i = 0;
		for (Map.Entry<String, Set<ComponentNode>> me : components.entrySet())
			for (ComponentNode node : me.getValue())
				sList.add(node.getName());
				// sArray[i++] = node.getName();
		String[] sArray = sList.toArray(new String[sList.size()]);
		Arrays.sort(sArray, String.CASE_INSENSITIVE_ORDER);
		return sArray;
	}

	// Should see about these array producing functions only returning
	// either a list, or simply superclass of both lists and arrays
	// (iteratable? or something).
	public ComponentNode[] getComponents() {
		String[] sArray1 = components.keySet().toArray(new String[] {});
		Arrays.sort(sArray1, String.CASE_INSENSITIVE_ORDER);
		String[] sArray2;
		LinkedList<ComponentNode> speciesList = new LinkedList<ComponentNode>();
		Map<String, ComponentNode> map = new HashMap<String, ComponentNode>();
		for (String s : sArray1) {
			map.clear();
			for (ComponentNode cn : components.get(s)) {
				map.put(cn.getName(), cn);
			}
			sArray2 = map.keySet().toArray(new String[] {});
			Arrays.sort(sArray2, String.CASE_INSENSITIVE_ORDER);
			for (String s2 : sArray2)
				speciesList.addLast(map.get(s2));
		}
		ComponentNode[] speciesArray =
			speciesList.toArray(new ComponentNode[speciesList.size()]);
		return speciesArray;
	}

	public ComponentNode getNamedComponent(String componentName){
		ComponentNode[] componentNodes = getComponents();
		for (ComponentNode compNode : componentNodes) {
			if (compNode.getName().equals(componentName)) {
				return compNode;
			}
		}
		// Maybe we should actually raise an exception here
		return null;
	}
	
	public long getNamedComponentCount(String componentName) {
		ComponentNode compNode = this.getNamedComponent(componentName);
		if (compNode != null){
			return compNode.getCount();
		} else {
			// Again maybe we should raise an exception here, even if
			// we don't for 'getNamedComponent'
			return 0;
		}
	}

	/* Should this throw an exception if the we don't find the component? */
	public void setComponentCount(String componentName, long newCount) {
		for (ComponentNode compNode : getComponents()) {
			if (componentName.equals(compNode.getName())) {
				compNode.setCount(newCount);
				return;
			}
		}
	}

	public CompiledExpression getDynamicExpression(String name) {
		return dynamicVariables.get(name);
	}

	boolean inline(String name) {
		return !dontInline.contains(name);
	}

	public CompiledExpression getStaticExpression(String name) {
		if (dynamicVariables.containsKey(name))
			return null;
		VariableData vd = compiledBioPEPA.getVariableData(name);
		if (vd != null)
			return vd.getValue();
		return null;
	}

	public String[] getDynamicVariableNames() {
		String[] sArray = dynamicVariables.keySet().toArray(new String[] {});
		Arrays.sort(sArray, String.CASE_INSENSITIVE_ORDER);
		return sArray;
	}

	public SBAReaction[] getReactions() {
		String[] sArray = reactions.keySet().toArray(new String[] {});
		Arrays.sort(sArray, String.CASE_INSENSITIVE_ORDER);
		SBAReaction[] reactionArray = new SBAReaction[sArray.length];
		for (int i = sArray.length - 1; i >= 0; i--)
			reactionArray[i] = reactions.get(sArray[i]);
		return reactionArray;
	}

	public void parseBioPEPA() {
		SystemEquationVisitor sev = new SystemEquationVisitor();
		sev.visit(compiledBioPEPA.getSystemEquation());
		Set<String> used = new HashSet<String>();
		for (VariableData vd : compiledBioPEPA.getDynamicVariables()) {
			dynamicVariables.put(vd.getName(), vd.getValue());
			used.add(vd.getName());
			if (vd.getUsage() > 1)
				dontInline.add(vd.getName());
		}
		// Singleton reactions which do not need to be renamed
		List<SBAReaction> lSBAR;
		for (Map.Entry<String, List<SBAReaction>> me : sev.currentReactions.entrySet()) {
			lSBAR = me.getValue();
			if (lSBAR.size() == 1 && !lSBAR.get(0).isReversible()) {
				reactions.put(me.getKey(), me.getValue().get(0));
				used.add(me.getKey());
			}
		}
		Pattern p;
		ArrayList<Integer> numbers;
		int[] intArray;
		String name;
		int i;
		// Multiple reactions which require renaming.
		for (Map.Entry<String, List<SBAReaction>> me : sev.currentReactions.entrySet()) {
			lSBAR = me.getValue();
			if (lSBAR.size() > 1 || lSBAR.get(0).isReversible()) {
				// multiple reactions needing renamed
				name = me.getKey();
				i = name.length() + 1;
				numbers = new ArrayList<Integer>();
				p = Pattern.compile(name + "_\\d+");
				for (String existing : used)
					if (p.matcher(existing).matches())
						numbers.add(new Integer(existing.substring(i)));
				if (numbers.size() > 0) {
					intArray = new int[numbers.size()];
					for (int index = 0; index < intArray.length; index++)
						intArray[index] = numbers.get(index);
					Arrays.sort(intArray);
					i = intArray[intArray.length - 1] + 1;
				} else {
					i = 1;
					if (used.contains(name))
						i++;
				}
				for (SBAReaction r : lSBAR) {
					if (r.isReversible()) {
						if (used.contains(name))
							r.name = name + "_" + i;
						r.forwardName = name + "_" + i++;
						r.reversibleName = name + "_" + i++;
						used.add(r.forwardName);
						used.add(r.reversibleName);
					} else {
						r.name = name + "_" + i++;
					}
					reactions.put(r.name, r);
					used.add(r.name);
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (compartments.size() > 0) {
			sb.append("// Compartments\n");
			for (Map.Entry<String, Double> me : compartments.entrySet())
				sb.append(me.getKey()).append(" = ").append(me.getValue()).append(";\n");
			sb.append("\n");
		}
		sb.append("// Components\n");
		for (ComponentNode cn : getComponents())
			sb.append(cn.getName()).append(" = ").append(cn.getCount()).append(";\n");
		sb.append("\n");
		sb.append("// Reactions\n");
		for (SBAReaction r : reactions.values())
			sb.append(r.toString()).append(";\n");
		return sb.toString();
	}

	public boolean containsComponent(String name) {
		for (String vName : this.getComponentNames()){
			if (vName.equals(name)){
				return true;
			}
		}
		return false;
	}

	public boolean containsVariable(String name) {
		for (String vName : this.getDynamicVariableNames()){
			if (vName.equals(name)){
				return true;
			}
		}	
		return false;
	}
}
