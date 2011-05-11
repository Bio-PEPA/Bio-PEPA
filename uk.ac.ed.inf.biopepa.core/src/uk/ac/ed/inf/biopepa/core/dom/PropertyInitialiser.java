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
public class PropertyInitialiser extends Expression {

	private List<Expression> properties = new ASTNode.NodeList<Expression>();

	PropertyInitialiser(AST ast) {
		super(ast);
	}

	public List<Expression> properties() {
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTNode#accept(uk.ac.ed.inf.biopepa.core
	 * .dom.ASTVisitor)
	 */
	@Override
	public void accept(ASTVisitor visitor) throws BioPEPAException {
		visitor.visit(this);
	}

	/* A property initialiser cannot as far as I know contain a declaration Name */
	public void fillInDeclarationName(Expression _declName) {
		return;
	}
}
