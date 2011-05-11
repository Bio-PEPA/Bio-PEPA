/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotting;

/**
 * Interface for accessing UI-specific functionality
 * for charts
 * @author mtribast
 *
 */
public interface IPlottingToolsUI {

	IFormatAction[] getActions(IChart chart);
	
	void changeTitle(IChart chart, String newTitle);
	
}
