/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/

package uk.ac.ed.inf.common.ui.plotview.views.actions;

import org.eclipse.jface.viewers.IStructuredSelection;

import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotview.views.PlotView;

/**
 * @author mtribast
 *
 */
public class CloseTabAction extends PlotViewAction {

	public CloseTabAction(PlotView view) {
		super(view);
		setText("Close");
		setToolTipText("Close tab");
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.common.ui.plotview.views.PlotViewAction#doRun(org.eclipse.swt.widgets.TabItem[])
	 */
	@Override
	protected void doRun(IStructuredSelection selection) {
		view.close((IChart) selection.getFirstElement());
	}

}
