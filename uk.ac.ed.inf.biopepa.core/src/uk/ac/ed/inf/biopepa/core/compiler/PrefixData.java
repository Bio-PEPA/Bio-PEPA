/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.dom.ASTNode;

public abstract class PrefixData {

	public enum Operator {
		INHIBITOR, PRODUCT, REACTANT, GENERIC, ACTIVATOR, UNI_TRANSPORTATION, BI_TRANSPORTATION;
	}

	String function;

	long stoichometry = 1;

	Operator operator = null;

	ASTNode declaration;

	public String getFunction() {
		return function;
	}

	void setFunction(String function) {
		this.function = function;
	}

	public long getStoichometry() {
		return stoichometry;
	}

	void setStoichometry(long stoichometry) {
		this.stoichometry = stoichometry;
	}

	public Operator getOperator() {
		return operator;
	}

	void setOperator(Operator operator) {
		this.operator = operator;
	}
}
