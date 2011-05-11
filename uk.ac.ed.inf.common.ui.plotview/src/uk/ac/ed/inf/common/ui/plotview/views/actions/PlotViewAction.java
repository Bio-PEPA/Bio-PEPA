/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/

package uk.ac.ed.inf.common.ui.plotview.views.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import uk.ac.ed.inf.common.ui.plotview.views.PlotView;

/**
 * @author mtribast
 * 
 */
public abstract class PlotViewAction extends Action implements
		ISelectionChangedListener {

	protected PlotView view;

	public PlotViewAction(PlotView view) {
		this.view = view;
		view.addSelectionChangedListener(this);
		checkEnabled((IStructuredSelection) view.getSelection()); 
	}

	public final void run() {
		doRun((IStructuredSelection) view.getSelection());
	}
	
	protected abstract void doRun(IStructuredSelection selection);

	public void selectionChanged(SelectionChangedEvent event) {
		checkEnabled((IStructuredSelection) event.getSelection());
	}

	public void checkEnabled(IStructuredSelection selection) {
		setEnabled(!selection.isEmpty());
	}

}
