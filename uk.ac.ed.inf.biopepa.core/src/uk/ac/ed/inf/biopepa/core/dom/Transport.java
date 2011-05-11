/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * 
 * @author ajduguid
 * 
 */
public class Transport extends InfixExpression {

	private Name actionType;

	Transport(AST ast) {
		super(ast);
	}

	public void setLeftHandSide(Expression leftHandSide) {
		if (!(leftHandSide instanceof Name))
			throw new IllegalArgumentException();
		super.setLeftHandSide(leftHandSide);
	}

	public void setOperator(Operator operand) {
		if (!(operand.equals(Operator.UMOVE) || operand.equals(Operator.BMOVE)))
			throw new IllegalArgumentException();
		super.setOperator(operand);
	}

	public void setRightHandSide(Expression RightHandSide) {
		if (!(RightHandSide instanceof Name))
			throw new IllegalArgumentException();
		super.setRightHandSide(RightHandSide);
	}

	public void setActionType(Name actionType) {
		this.actionType = actionType;
	}

	public Name getActonType() {
		return actionType;
	}

	@Override
	public void accept(ASTVisitor visitor) throws BioPEPAException {
		visitor.visit(this);
	}

}
