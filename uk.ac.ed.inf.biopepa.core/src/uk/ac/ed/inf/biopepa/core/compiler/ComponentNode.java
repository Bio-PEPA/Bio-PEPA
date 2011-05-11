/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.dom.AST;

/**
 * A component in the system equation.
 * 
 * @author Mirco
 * 
 */
public class ComponentNode extends SystemEquationNode {

	private String component;

	CompartmentData compartment = null;

	private long molecularCount;
	private CompiledExpression initialAmountExpression;

	ComponentNode (CompiledExpression init){
		this.initialAmountExpression = init;
	}

	/*
	 * Return the initial amount expression, this is generally useful
	 * for exporting such as to SBML where the initial expression is
	 * useful for allowing experimentation over sub-expressions of the
	 * initial amount expression.
	 * For example if I have
	 * A[x * y]
	 * I might wish to range over values for x which in turn changes the
	 * initial amount of A.
	 */
	public CompiledExpression getInitialAmountExpression (){
		return this.initialAmountExpression;
	}
	
	public String getComponent() {
		return component;
	}

	void setComponent(String component) {
		this.component = component;
	}

	/**
	 * returns null if compartments are not explicitly defined in the model.
	 * 
	 * @return
	 */
	public CompartmentData getCompartment() {
		return compartment;
	}

	void setCompartment(CompartmentData compartment) {
		this.compartment = compartment;
	}

	public long getCount() {
		return molecularCount;
	}

	public void setCount(long count) {
		this.molecularCount = count;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(component);
		if (compartment != null)
			buf.append("@").append(compartment);
		buf.append("[");
		buf.append(molecularCount);
		buf.append("]");
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.inf.biopepa.core.compiler.SystemEquationNode#getType()
	 */
	public int getType() {
		return SystemEquationNode.COMPONENT;
	}

	public String getName() {
		if (compartment == null)
			return component;
		return component + AST.Literals.LOCATION + compartment.getName();
	}
}
