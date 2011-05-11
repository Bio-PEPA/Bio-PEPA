/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * A cooperation is a kind of expression to build the system equation of a
 * BioPEPA model.
 * 
 * @author mtribast
 * 
 */
public class Cooperation extends Expression {

	public static final String WILDCARD = "*";

	private Expression leftHandSide;

	private Expression rightHandSide;

	private NameSet actionSet;

	Cooperation(AST ast) {
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

	/**
	 * @return the leftHandSide
	 */
	public Expression getLeftHandSide() {
		return leftHandSide;
	}

	/**
	 * @param leftHandSide
	 *            the leftHandSide to set
	 */
	public void setLeftHandSide(Expression leftHandSide) {
		this.leftHandSide = leftHandSide;
	}

	/**
	 * @return the rightHandSide
	 */
	public Expression getRightHandSide() {
		return rightHandSide;
	}

	/**
	 * @param rightHandSide
	 *            the rightHandSide to set
	 */
	public void setRightHandSide(Expression rightHandSide) {
		this.rightHandSide = rightHandSide;
	}

	/**
	 * @return the actionSet
	 */
	public NameSet getActionSet() {
		return actionSet;
	}

	/**
	 * @param actionSet
	 *            the actionSet to set
	 */
	public void setActionSet(NameSet actionSet) {
		this.actionSet = actionSet;
	}

	/* The system equation cannot have any declaration names */
	public void fillInDeclarationName(Expression _declName) {
		return;
	}

}
