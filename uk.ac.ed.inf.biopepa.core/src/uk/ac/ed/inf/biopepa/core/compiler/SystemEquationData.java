/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.dom.ASTNode;

/**
 * @author Mirco
 * 
 */
public class SystemEquationData extends Data {

	private SystemEquationNode node;

	protected SystemEquationData(String name, ASTNode declaration) {
		super(name, declaration);
	}

	public SystemEquationNode getSystemEquationNode() {
		return node;
	}

	void setSystemEquationNode(SystemEquationNode node) {
		this.node = node;
	}

}
