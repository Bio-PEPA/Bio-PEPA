/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * @author mtribast
 * 
 */
@SuppressWarnings("serial")
public class CompilerException extends BioPEPAException {

	public CompilerException(String message) {
		super(message);
	}

	public CompilerException() {
		super("");
	}

}
