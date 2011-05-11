/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import java.util.List;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * @author mtribast
 * 
 */
public class Model extends ASTNode {

	private ASTNode.NodeList<Statement> statements = new ASTNode.NodeList<Statement>();

	Model(AST ast) {
		super(ast);
	}

	public List<Statement> statements() {
		return statements;
	}
	
	@Override
	public void accept(ASTVisitor visitor) throws BioPEPAException {
		visitor.visit(this);
	}

}
