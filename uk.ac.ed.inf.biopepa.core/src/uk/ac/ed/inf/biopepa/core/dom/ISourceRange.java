/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

/**
 * @author Mirco
 * @author ajd
 * 
 */
public interface ISourceRange {

	/**
	 * Position of the first character in the source stream
	 * 
	 * @return 0-relative position of the first character
	 */
	int getChar();

	/**
	 * Line number
	 * 
	 * @return
	 */
	int getLine();

	/**
	 * Column index (char index of line)
	 * 
	 * @return
	 */
	int getColumn();

	/**
	 * The number of characters
	 * 
	 * @return the number of characters
	 */
	int getLength();

}
