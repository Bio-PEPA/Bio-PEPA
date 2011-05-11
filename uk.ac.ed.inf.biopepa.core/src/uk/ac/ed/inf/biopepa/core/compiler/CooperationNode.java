/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

/**
 * @author Mirco
 * 
 */
public class CooperationNode extends SystemEquationNode {

	private SystemEquationNode left = null;

	private SystemEquationNode right = null;

	private String[] actions = new String[0];

	public SystemEquationNode getLeft() {
		return left;
	}

	void setLeft(SystemEquationNode left) {
		left.setParent(this);
		this.left = left;
	}

	public SystemEquationNode getRight() {
		return right;
	}

	void setRight(SystemEquationNode right) {
		right.setParent(this);
		this.right = right;
	}

	void setActions(String[] actions) {
		this.actions = actions;
	}

	public String[] getActions() {
		return this.actions;
	}

	@Override
	public int getType() {
		return SystemEquationNode.COOPERATION;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("(L:");
		buf.append(left);
		buf.append("<");
		for (String action : actions) {
			buf.append(action);
			buf.append(" ");

		}
		buf.append(">");
		buf.append(" R:");
		buf.append(right);
		buf.append(")");
		return buf.toString();
	}

}
