/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.dom.ASTNode;
import uk.ac.ed.inf.biopepa.core.dom.PropertyLiteral;

/**
 * Information about a species
 * 
 * @author mtribast
 * 
 */
public class SpeciesData extends Data {

	// private double initialConcentration = Double.NaN;

	private long maximumCount = Long.MIN_VALUE;

	private long minimalCount = Long.MIN_VALUE;

	private CompartmentData compartment = null;

	SpeciesData(String name, ASTNode declaration) {
		super(name, declaration);
	}

	boolean isSetProperty(PropertyLiteral literal) {
		return isSetProperty(literal.getKind());

	}

	boolean isSetProperty(PropertyLiteral.Kind kind) {
		if (kind == null)
			throw new IllegalArgumentException();
		if (kind == PropertyLiteral.Kind.MAX)
			return maximumCount != Long.MIN_VALUE;
		if (kind == PropertyLiteral.Kind.MIN)
			return minimalCount != Long.MIN_VALUE;
		if (kind == PropertyLiteral.Kind.V)
			return null != compartment;
		throw new IllegalArgumentException();
	}

	void setProperty(PropertyLiteral literal, long result) throws PropertySetterException {
		if (result < 0)
			throw new PropertySetterException(ProblemKind.GTE_ZERO);
		if (literal.getKind() == PropertyLiteral.Kind.MAX) {
			if (minimalCount != Long.MIN_VALUE && result < minimalCount)
				throw new PropertySetterException(ProblemKind.MAX_LT_MIN);
			maximumCount = result;
			return;
		}
		if (literal.getKind() == PropertyLiteral.Kind.MIN) {
			if (maximumCount != Long.MIN_VALUE && result >= maximumCount)
				throw new PropertySetterException(ProblemKind.MIN_GT_MAX);
			minimalCount = result;
			return;
		}
		throw new PropertySetterException(ProblemKind.ILLEGAL_PROPERTY);
	}

	/*
	 * void setInitialConcentration(int value) throws PropertySetterException{
	 * if(value < 0) throw new IllegalArgumentException();
	 * if(!Double.isNaN(maximumCount) && value > maximumCount) throw new
	 * PropertySetterException(ProblemKind.INITIAL_CONCENTRATION_GT_MAX);
	 * if(!Double.isNaN(minimalCount) && value < minimalCount) throw new
	 * PropertySetterException(ProblemKind.INITIAL_CONCENTRATION_LT_MIN);
	 * initialConcentration = value; }
	 * 
	 * public double getInitialConcentration() {return initialConcentration;}
	 */

	public long getMaximumConcentration() {
		return maximumCount;
	}

	public long getMinimalConcentration() {
		return minimalCount;
	}

	public CompartmentData getCompartment() {
		return compartment;
	}

	void setCompartment(CompartmentData compartmentData) {
		this.compartment = compartmentData;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[Species] Name=");
		buf.append(getName());
		// buf.append(",InitialConcentration=");
		// buf.append(getInitialConcentration());
		buf.append(",MaximumMolecularCount=");
		buf.append(getMaximumConcentration());
		buf.append(",MinimalMolecularCount=");
		buf.append(getMinimalConcentration());
		buf.append(",Compartment=");

		buf.append(((compartment != null) ? compartment.getName() : "null"));
		return buf.toString();

	}

	public int compareTo(Object o) {
		SpeciesData s = (SpeciesData) o;
		int i = compartment.name.compareTo(s.compartment.name);
		if (i != 0)
			return i;
		return name.compareTo(s.name);
	}

}
