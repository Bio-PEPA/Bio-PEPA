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
 * Represents a function call
 * 
 * @author mtribast
 * 
 */
public class FunctionCall extends Expression {

	private Name name;

	private ASTNode.NodeList<Expression> arguments = new ASTNode.NodeList<Expression>();

	/**
	 * @return the name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}

	/**
	 * @return the arguments
	 */
	public List<Expression> arguments() {
		return arguments;
	}

	FunctionCall(AST ast) {
		super(ast);
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

	/* A function call have any declaration names */
	public void fillInDeclarationName(Expression _declName) {
		return;
	}

}
