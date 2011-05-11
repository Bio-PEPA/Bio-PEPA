/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.dom.InfixExpression;

public class CompiledOperatorNode extends CompiledExpression {

	public enum Operator {

		PLUS("+"), MINUS("-"), DIVIDE("/"), MULTIPLY("*"), POWER("^");

		private String op;

		Operator(String s) {
			op = s;
		}

		public String toString() {
			return op;
		}
	}

	CompiledExpression left = null, right = null;

	Operator operator = null;

	public void setLeft(CompiledExpression left) {
		this.left = left;
	}

	public void setRight(CompiledExpression right) {
		this.right = right;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	void setOperator(InfixExpression.Operator operator) {
		switch (operator) {
		case PLUS:
			this.operator = Operator.PLUS;
			break;
		case MINUS:
			this.operator = Operator.MINUS;
			break;
		case TIMES:
			this.operator = Operator.MULTIPLY;
			break;
		case DIVIDE:
			this.operator = Operator.DIVIDE;
			break;
		case POWER:
			this.operator = Operator.POWER;
			break;
		default:
			// TODO
		}
	}

	public CompiledExpression getLeft() {
		return left;
	}

	public CompiledExpression getRight() {
		return right;
	}

	public Operator getOperator() {
		return operator;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (left == null){
			System.out.println ("Yeah left is null");
			System.out.println (right.toString());
		}
		if (operator == null){
			System.out.println ("Yeah operator is null");
			System.out.println (left.toString());
		}
		sb.append(left.toString()).append(operator.toString());
		sb.append(right.toString());
		return sb.toString();
	}

	public boolean accept(CompiledExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	public boolean isDynamic () {
		if (left != null && left.isDynamic()) {
			return true;
		}
		if (right != null && right.isDynamic()){
			return true;
		}
		return false;
	}
	
	public CompiledOperatorNode clone() {
		CompiledOperatorNode con = new CompiledOperatorNode();
		con.operator = operator;
		if (left != null)
			con.left = left.clone();
		if (right != null)
			con.right = right.clone();
		if (expandedForm != null)
			con.expandedForm = expandedForm.clone();
		return con;
	}
}
