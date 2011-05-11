/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * A BioPEPA prefix, such as:
 * 
 * <pre>
 *  (alpha, 1)
 * </pre>
 * 
 * @author mtribast
 * 
 */
public class Prefix extends Expression {

	private Expression actionType;

	private Expression stoichometry;

	/**
	 * @return the actionType
	 */
	public Expression getActionType() {
		return actionType;
	}

	/**
	 * @param actionType
	 *            the actionType to set
	 */
	public void setActionType(Expression actionType) {
		this.actionType = actionType;
	}

	/**
	 * @return the stoichometry
	 */
	public Expression getStoichometry() {
		return stoichometry;
	}

	/**
	 * @param stoichometry
	 *            the stoichometry to set
	 */
	public void setStoichometry(Expression stoichometry) {
		this.stoichometry = stoichometry;
	}

	Prefix(AST ast) {
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

	/* Prefix nodes cannot contain declaration names */
	public void fillInDeclarationName (Expression _declName){
		return ;
	}

}
