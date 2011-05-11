/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

/**
 * Connects a variable declaration to its usages.
 * 
 * @author mtribast
 * 
 */
public interface IBinding {

	/**
	 * Returns the variable declaration for this binding
	 * 
	 * @return the variable declaration for this binding
	 */
	public VariableDeclaration getVariableDeclaration();
}
