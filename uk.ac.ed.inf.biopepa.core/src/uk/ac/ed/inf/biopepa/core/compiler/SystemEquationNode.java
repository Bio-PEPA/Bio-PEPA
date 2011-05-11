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
public abstract class SystemEquationNode {

	public static final int COOPERATION = 0;

	public static final int COMPONENT = 1;

	protected SystemEquationNode parent = null;

	public abstract int getType();

	public SystemEquationNode getParent() {
		return parent;
	}

	protected void setParent(SystemEquationNode parent) {
		this.parent = parent;
	}

}
