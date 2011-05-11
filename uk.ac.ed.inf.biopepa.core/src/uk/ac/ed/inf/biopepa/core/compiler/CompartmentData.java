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
 * Compiled information about a compartment
 * 
 * @author mtribast
 * 
 */
public class CompartmentData extends Data {

	public enum Type {

		COMPARTMENT("Compartment", 3), MEMBRANE("Membrane", 2);

		private String name;
		private int dimensions;

		Type(String name, int dimensions) {
			this.name = name;
			this.dimensions = dimensions;
		}

		public String toString() {
			return name;
		}
		
		/*
		 * Format in such away that it will be recognised by the
		 * biopepa parser.
		 */
		public String format(){
			return name.toLowerCase();
		}

		public int getDimensions() {
			return dimensions;
		}
	}

	private double volume = Double.NaN, stepSize = Double.NaN;

	private Type type = Type.COMPARTMENT;

	private String parent = null;

	public double getVolume() {
		return volume;
	}

	public double getStepSize() {
		return stepSize;
	}

	public Type getType() {
		return type;
	}

	void setParent(String parent) {
		this.parent = parent;
	}

	public String getParent() {
		return parent;
	}

	protected CompartmentData(String name, ASTNode declaration) {
		super(name, declaration);
	}

	public void setProperty(PropertyLiteral literal, double result) throws PropertySetterException {
		if (literal.getKind() == PropertyLiteral.Kind.SIZE) {
			if (result < 0)
				throw new IllegalArgumentException();
			volume = result;
			return;
		} else if (literal.getKind() == PropertyLiteral.Kind.H) {
			if (result < 0)
				throw new IllegalArgumentException();
			stepSize = result;
			return;
		}
		throw new PropertySetterException(ProblemKind.ILLEGAL_PROPERTY);

	}

	// TODO
	public void setProperty(PropertyLiteral literal, PropertyLiteral result) throws PropertySetterException {
		if (literal.getKind() != PropertyLiteral.Kind.TYPE)
			throw new PropertySetterException(ProblemKind.ILLEGAL_PROPERTY);
		if (result.getKind() == PropertyLiteral.Kind.COMPARTMENT)
			type = Type.COMPARTMENT;
		else if (result.getKind() == PropertyLiteral.Kind.MEMBRANE)
			type = Type.MEMBRANE;
		else
			throw new PropertySetterException(ProblemKind.ILLEGAL_PROPERTY);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Compartment] Name=");
		sb.append(getName());
		sb.append(",Volume=");
		sb.append(this.volume);
		return sb.toString();
	}
}
