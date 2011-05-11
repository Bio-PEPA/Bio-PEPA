/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotting.internal;

import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotting.IFormatAction;
import uk.ac.ed.inf.common.ui.plotting.IPlottingToolsUI;

public class PlottingToolsUI implements IPlottingToolsUI {

	public IFormatAction[] getActions(IChart chart) {
		// TODO Auto-generated method stub
		return null;
	}

	public void changeTitle(IChart chart, String newTitle) {
		((CommonChart) chart).getBirtChart().getTitle().getLabel().getCaption()
				.setValue(newTitle);
	}

}
