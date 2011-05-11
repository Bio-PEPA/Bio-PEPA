/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.sba;

import uk.ac.ed.inf.biopepa.core.compiler.PrefixData.Operator;
import uk.ac.ed.inf.biopepa.core.dom.AST;

public class SBAComponentBehaviour implements Comparable<SBAComponentBehaviour> {

	private static final String IN = AST.Literals.LOCATION.getToken();

	public enum Type {
		REACTANT("reactant"), PRODUCT("product"), CATALYST("catalyst"), INHIBITOR("inhibitor"), MODIFIER("modifier");

		String name;

		Type(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

	String name, compartment = null;
	Type type;
	int stoichiometry = 1;

	SBAComponentBehaviour(String name, String compartment, Type type) {
		if (name == null || type == null)
			throw new IllegalArgumentException("Component and type must be declared at initialisation.");
		this.name = name;
		this.compartment = compartment;
		this.type = type;
	}

	SBAComponentBehaviour(String name, String compartment, Operator operator) {
		if (name == null || operator == null)
			throw new IllegalArgumentException("Component and operator must be declared at initialisation.");
		this.name = name;
		this.compartment = compartment;
		if (operator.equals(Operator.REACTANT))
			type = Type.REACTANT;
		else if (operator.equals(Operator.PRODUCT))
			type = Type.PRODUCT;
		else if (operator.equals(Operator.ACTIVATOR))
			type = Type.CATALYST;
		else if (operator.equals(Operator.INHIBITOR))
			type = Type.INHIBITOR;
		else if (operator.equals(Operator.GENERIC))
			type = Type.MODIFIER;
		else
			throw new IllegalArgumentException("Unknown Operator.");
	}

	public SBAComponentBehaviour clone() {
		SBAComponentBehaviour clone = new SBAComponentBehaviour(name, compartment, type);
		clone.stoichiometry = stoichiometry;
		return clone;
	}

	/*
	 * @Override public int compareTo(Object o) { return
	 * component.compareTo(((SBAComponentBehaviour) o).component); }
	 */

	public int compareTo(SBAComponentBehaviour c) {
		return getName().compareTo(c.getName());
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SBAComponentBehaviour))
			return false;
		return (compareTo((SBAComponentBehaviour) o) == 0);
	}

	public String getName() {
		if (compartment == null)
			return name;
		return name + IN + compartment;
	}

	public int getStoichiometry() {
		return stoichiometry;
	}

	public Type getType() {
		return type;
	}

	public int hashCode() {
		return getName().hashCode();
	}

	void setStoichiometry(int stoichiometry) {
		if (!(type.equals(Type.REACTANT) || type.equals(Type.PRODUCT)) && stoichiometry != 1)
			throw new UnsupportedOperationException("Stoichiometry is not supported for components of type "
					+ type.toString() + ".");
		if (stoichiometry < 1)
			throw new IllegalArgumentException("Stoichiometric values must be greater than zero.");
		this.stoichiometry = stoichiometry;
	}

	public String toCMDL() {
		boolean dollar = type.equals(Type.CATALYST) || type.equals(Type.INHIBITOR) || type.equals(Type.MODIFIER);
		StringBuilder sb = new StringBuilder();
		for (int i = stoichiometry; i > 0; i--) {
			if (dollar)
				sb.append("$");
			sb.append(getName()).append(" + ");
		}
		sb.delete(sb.length() - 3, sb.length());
		return sb.toString();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (stoichiometry > 1)
			sb.append(stoichiometry).append(".");
		sb.append((type.equals(Type.CATALYST) || type.equals(Type.INHIBITOR) || type.equals(Type.MODIFIER)) ? "$" : "");
		sb.append(getName());
		return sb.toString();
	}
	
	public String formatType(){
		switch (this.type){
			case REACTANT : return "<<";
			case PRODUCT : return ">>";
			case CATALYST: return "(+)";
			case INHIBITOR: return "(-)";
			case MODIFIER: return "(.)";
			default: return "(unknown type)";
		}
	}
	
	/*
	 * Format such that it may be placed into a model, that is
	 * format this component behaviour in BioPEPA.
	 */
	public String format(String reactionName){
		StringBuilder sb = new StringBuilder();
		if (stoichiometry == 1){
			sb.append(reactionName);
			sb.append (" ");
			sb.append (formatType());
		} else {
			sb.append("(");
			sb.append(reactionName);
			sb.append (", ");
			sb.append(stoichiometry);
			sb.append(") ");
			sb.append (formatType());
		}
		if (this.compartment != null){
		  sb.append(" ");
		  sb.append(this.name);
		  sb.append("@");
		  sb.append(this.compartment);
		}
		
		return sb.toString();
	}
}
