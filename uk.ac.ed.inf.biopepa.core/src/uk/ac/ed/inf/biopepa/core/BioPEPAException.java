/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core;

/**
 * An exception thrown by handlers of BioPEPA models
 * 
 * @author Mirco
 * 
 */
@SuppressWarnings("serial")
public class BioPEPAException extends Exception {

	public BioPEPAException(String message) {
		super(message);
	}

	public BioPEPAException(Throwable e) {
		super(e);
	}

}
