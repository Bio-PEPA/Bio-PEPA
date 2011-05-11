/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotting;

/**
 * A action for plot formatting
 * 
 * @author mtribast
 *
 */
public interface IFormatAction {
	
	/**
	 * A forward slash-separated category path, the last fragment
	 * represents the action id. An action must contain at least
	 * two fragments
	 * @return
	 */
	String getCategory();
	
	/**
	 * Human-readable label for this action 
	 * @return
	 */
	String getLabel();
	
	/**
	 * A more verbose human-readable description for 
	 * this action.
	 * @return
	 */
	String getDescription();
	
}
