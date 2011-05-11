/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.dom.ASTNode;

/**
 * Compiled variables
 * 
 * @author mtribast
 * 
 */
public class VariableData extends Data {

	private CompiledExpression value;

	public VariableData(String name, ASTNode declaration) {
		super(name, declaration);
	}

	public CompiledExpression getValue() {
		return value;
	}

	void setValue(CompiledExpression expression) {
		this.value = expression;
	}

	public boolean isDynamic() {
		return value.isDynamic();
	}

}
