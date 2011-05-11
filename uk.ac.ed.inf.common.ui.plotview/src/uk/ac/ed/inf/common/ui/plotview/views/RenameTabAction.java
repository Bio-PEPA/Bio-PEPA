/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/

package uk.ac.ed.inf.common.ui.plotview.views;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.TabItem;

import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotview.views.actions.PlotViewAction;

/**
 * @author mtribast
 * 
 */
public class RenameTabAction extends PlotViewAction {

	public RenameTabAction(PlotView view) {
		super(view);
		this.setText("Rename");
		this.setToolTipText("Rename tab");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.inf.common.ui.plotview.views.PlotViewAction#doRun(org.eclipse.swt.widgets.TabItem[])
	 */
	@Override
	protected void doRun(IStructuredSelection selection) {
		IChart chart = (IChart) selection.getFirstElement();
		TabItem selectedItem = view.getTab(chart);
		InputDialog dialog = new InputDialog(selectedItem.getControl()
				.getShell(), "Rename Tab", "Enter new name", selectedItem
				.getText(), new IInputValidator() {

			public String isValid(String newText) {
				return newText.equals("") ? "Please insert a valid name" : null;
			}

		});
		if (dialog.open() == InputDialog.OK)
			selectedItem.setText(dialog.getValue().trim());
	}

}
