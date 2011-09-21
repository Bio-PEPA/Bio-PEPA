/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.sba;

import java.util.*;

import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpression;
import uk.ac.ed.inf.biopepa.core.compiler.TransportData;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour.Type;

public class SBAReaction {

	String name, reversibleName, forwardName;
	private String originalName;
	boolean reversible = false;
	private boolean enabled = true; // By default a reaction is enabled.

	// The permanence of this variable is open for discussion, hence it not
	// gaining public accessor methods. See line ~98 for usage.
	TransportData transportation = null;

	List<SBAComponentBehaviour> reactants, products;

	CompiledExpression reactionRate;

	SBAReaction(String name, CompiledExpression rate) {
		reactants = new LinkedList<SBAComponentBehaviour>();
		products = new LinkedList<SBAComponentBehaviour>();
		originalName = name;
		this.name = name;
		reactionRate = rate;
	}

	public String getName() {
		return name;
	}

	public List<SBAComponentBehaviour> getReactants() {
		List<SBAComponentBehaviour> newList = new LinkedList<SBAComponentBehaviour>();
		newList.addAll(reactants);
		return newList;
	}

	public List<SBAComponentBehaviour> getProducts() {
		List<SBAComponentBehaviour> newList = new LinkedList<SBAComponentBehaviour>();
		newList.addAll(products);
		return newList;
	}

	public CompiledExpression getRate() {
		return reactionRate;
	}

	void setReversible(boolean reversible) {
		this.reversible = reversible;
		if (reversible) {
			for (SBAComponentBehaviour cb : reactants)
				if (!cb.type.equals(Type.REACTANT))
					throw new IllegalStateException("");
		}
	}

	public boolean isReversible() {
		return reversible;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	boolean addComponent(SBAComponentBehaviour c) {
		if (c == null)
			throw new NullPointerException("SBAComponent must be non-null.");
		if (c.type.equals(Type.PRODUCT)) {
			if (products.contains(c))
				return false;
			return products.add(c);
		}
		if (reactants.contains(c))
			return false;
		if (reversible && !c.type.equals(Type.REACTANT))
			throw new IllegalStateException("");
		return reactants.add(c);
	}

	public SBAReaction clone() {
		SBAReaction r = new SBAReaction(originalName, reactionRate);
		r.name = name;
		for (SBAComponentBehaviour cb : reactants)
			r.reactants.add(cb.clone());
		for (SBAComponentBehaviour cb : products)
			r.products.add(cb.clone());
		r.reversible = reversible;
		r.transportation = transportation;
		return r;
	}

	public static List<SBAReaction> merge(SBAReaction one, SBAReaction two) {
		if (one == null)
			throw new NullPointerException("First SBAReaction cannot be null.");
		if (two == null)
			throw new NullPointerException("Second SBAReaction cannot be null.");
		if (!one.originalName.equals(two.originalName))
			throw new IllegalArgumentException("Can only merge on identical action names.");
		if (one.reversible != two.reversible)
			throw new IllegalArgumentException("Cannot merge reversible and non-reversible reactions.");
		if (one.transportation != two.transportation)
			throw new IllegalArgumentException("Cannot merge transportation reactions.");
		
		/*
		 * This is where we need to solve our locations bug.
		 * These reactions should not be blindly merged. Previously this method had returned
		 * a single reaction, I've changed the types, but the semantics are still the same.
		 * We need to return more than one reaction here. It depends on what sides have what
		 * located species etc.
		 */
		SBAReaction r = one.clone();
		r.reversible = one.reversible;
		for (SBAComponentBehaviour cb : two.reactants)
			r.reactants.add(cb);
		for (SBAComponentBehaviour cb : two.products)
			r.products.add(cb);
		
		LinkedList<SBAReaction> result = new LinkedList<SBAReaction>();
		result.add(r);
		return result;
	}

	/*
	 * Gives the net affect of the current reaction on the given
	 * named component. For example if the reaction is
	 * A+A -> A+B then the netAffect of A is -1 and for B is +1.
	 */
	public int netAffect(String componentName){
		int net = 0;
		for (SBAComponentBehaviour reactant : this.reactants){
			if (reactant.getName().equals(componentName)){
				net -= reactant.stoichiometry;
			}
		}
		for (SBAComponentBehaviour product : this.products){
			if (product.getName().equals(componentName)){
				net += product.stoichiometry;
			}
		}
		
		return net;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(", ");
		for (SBAComponentBehaviour cb : reactants)
			sb.append(cb.toString()).append(" + ");
		if (reactants.size() > 0)
			sb.delete(sb.length() - 3, sb.length());
		if (reversible)
			sb.append("  <->  ");
		else
			sb.append("  ->  ");
		for (SBAComponentBehaviour cb : products)
			sb.append(cb.toString()).append(" + ");
		if (products.size() > 0)
			sb.delete(sb.length() - 3, sb.length());
		return sb.toString();
	}
}
