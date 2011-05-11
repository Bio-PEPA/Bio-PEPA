/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * @author mtribast
 * 
 */
public class InfixExpression extends Expression {

	/**
	 * Enumeration of the operands admitted in a postfix expression
	 * 
	 * @author mtribast
	 * 
	 */
	public enum Operator {

		PRODUCT(AST.Literals.PRODUCT.getToken()), REACTANT(AST.Literals.REACTANT.getToken()), ACTIVATOR(
				AST.Literals.ACTIVATOR.getToken()), INHIBITOR(AST.Literals.INHIBITOR.getToken()), GENERIC(
				AST.Literals.GENERIC_MODIFIER.getToken()), PLUS(AST.Literals.PLUS.getToken()), MINUS(AST.Literals.MINUS
				.getToken()), DIVIDE(AST.Literals.DIVIDE.getToken()), TIMES(AST.Literals.TIMES.getToken()), POWER(
				AST.Literals.POWER.getToken()), EQUALS(AST.Literals.EQUALS.getToken()), UMOVE(AST.Literals.UMOVE
				.getToken()), BMOVE(AST.Literals.BMOVE.getToken());

		private String literal;

		Operator(String literal) {
			this.literal = literal;
		}

		public String getLiteral() {
			return literal;
		}

		@Override
		public String toString() {
			return getLiteral();
		}
	}

	private Expression leftHandSide;

	private Operator operator;

	private Expression rightHandSide;

	InfixExpression(AST ast) {
		super(ast);
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
	 * @return the operand
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * @param operand
	 *            the operand to set
	 */
	public void setOperator(Operator operand) {
		this.operator = operand;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.Expression#fillInDeclarationName(uk.ac.
	 * ed.inf.biopepa.core.dom.Expression) Note here that we only check if the
	 * right hand side for null as that is the only place in which it may be
	 * used. Actually we should check that the left hand side is a prefix node.
	 */
	public void fillInDeclarationName(Expression declName) {
		/*
		 * Really we should not use 'null' to represent the declaration name we
		 * should invent a new kind of expression call DeclName and use that
		 * together here with instance of
		 */
		if (rightHandSide == null) {
			rightHandSide = declName;
		} else {
			leftHandSide.fillInDeclarationName(declName);
			rightHandSide.fillInDeclarationName(declName);
		}
	}
}
