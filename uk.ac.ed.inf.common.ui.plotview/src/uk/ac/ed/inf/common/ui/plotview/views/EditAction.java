/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotview.views;

import org.eclipse.jface.dialogs.DialogTray;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import uk.ac.ed.inf.common.ui.plotview.views.actions.PlotViewAction;

public class EditAction extends PlotViewAction {

	public EditAction(PlotView view) {
		super(view);
		setDescription("Edit");
		setToolTipText("Edit this graph");
	}

	@Override
	protected void doRun(IStructuredSelection selection) {
		//IChart chart = (IChart) selection.getFirstElement();
		 
		TrayDialog dialog = new TrayDialog(view.getPage().getActivePart().getSite().getShell()) {
			
		};
		DialogTray tray = new DialogTray() {

			@Override
			protected Control createContents(Composite parent) {
				Composite main = new Composite(parent, SWT.NONE);
				main.setLayout(new GridLayout());
				new Label(main, SWT.NULL).setText("Test");
				return main;
			}
			
		};
		dialog.openTray(tray);
		dialog.open();
	}
}
