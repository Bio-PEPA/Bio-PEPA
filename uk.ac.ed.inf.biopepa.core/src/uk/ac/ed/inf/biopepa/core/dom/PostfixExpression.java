/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * A postfix expression. For instance,
 * 
 * <pre>
 * (alpha, 1) (+)
 * </pre>
 * 
 * @author mtribast
 * 
 */
public class PostfixExpression extends Expression {

	/**
	 * Enumeration of the operands admitted in a postfix expression
	 * 
	 * @author mtribast
	 * 
	 */
	public enum Operator {

		PRODUCT(AST.Literals.PRODUCT.getToken()),

		REACTANT(AST.Literals.REACTANT.getToken()),

		ACTIVATOR(AST.Literals.ACTIVATOR.getToken()),

		INHIBITOR(AST.Literals.INHIBITOR.getToken()),

		GENERIC(AST.Literals.GENERIC_MODIFIER.getToken());

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

	private Expression operand;

	private Operator operator;

	PostfixExpression(AST ast) {
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
	 * Gets the operand
	 * 
	 * @return the operand
	 */
	public Expression getOperand() {
		return operand;
	}

	/**
	 * Sets the operand
	 * 
	 * @param operand
	 *            the operand to set
	 */
	public void setOperand(Expression operand) {
		this.operand = operand;
	}

	/**
	 * Gets the operator
	 * 
	 * @return the operator
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * Sets the operator
	 * 
	 * @param operator
	 *            the operator to set.
	 */
	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	@Override
	public void fillInDeclarationName(Expression declName) {
		// TODO Auto-generated method stub

	}

}
