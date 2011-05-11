/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.dom.ASTNode;

public class FunctionalRateData extends Data {

	private CompiledExpression expression;
	boolean predefinedLaw;

	protected FunctionalRateData(String name, ASTNode declaration) {
		super(name, declaration);
	}

	public CompiledExpression getRightHandSide() {
		return expression;
	}

	void setRightHandSide(CompiledExpression expression) {
		this.expression = expression;
	}

	public boolean isPredefinedLaw() {
		return predefinedLaw;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[FunctionalRate] Name=");
		buf.append(getName());
		return buf.toString();
	}

}
