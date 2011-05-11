/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

public class CompiledNumber extends CompiledExpression {

	Number number;

	public CompiledNumber(Number number) {
		if (number == null)
			throw new NullPointerException();
		this.number = number;
	}

	public Number getNumber() {
		return number;
	}

	public boolean evaluatesToLong() {
		return (number instanceof Long);
	}

	public long longValue() {
		return number.longValue();
	}

	public boolean evaluatesToDouble() {
		return (number instanceof Double);
	}

	public double doubleValue() {
		return number.doubleValue();
	}

	public String toString() {
		return number.toString();
	}

	public boolean accept(CompiledExpressionVisitor visitor) {
		return visitor.visit(this);
	}
	
	public boolean isDynamic () { return false; }

	public CompiledNumber clone() {
		CompiledNumber cn = new CompiledNumber(number);
		if (expandedForm != null)
			cn.expandedForm = expandedForm.clone();
		return cn;
	}
}
