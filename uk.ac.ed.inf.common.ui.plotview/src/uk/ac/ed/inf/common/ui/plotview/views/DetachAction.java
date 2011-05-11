/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/

package uk.ac.ed.inf.common.ui.plotview.views;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;

import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotting.dialogs.ChartDialog;
import uk.ac.ed.inf.common.ui.plotview.views.actions.PlotViewAction;

/**
 * @author mtribast
 *
 */
class DetachAction extends PlotViewAction {

	public DetachAction(PlotView view) {
		super(view);
		setText("Detach");
		setToolTipText("Show chart in a separate dialog.");
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.inf.common.ui.plotview.views.PlotViewAction#doRun(org.eclipse.swt.widgets.TabItem[])
	 */
	@Override
	protected void doRun(IStructuredSelection selection) {
		IChart selectedChart = (IChart) selection.getFirstElement();
		TabItem selectedItem = view.getTab(selectedChart);
		final String cachedName = selectedItem.getText();
		Shell cachedShell = selectedItem.getControl().getShell();
		Point originalSize = selectedItem.getControl().getSize();
		view.close(selectedChart);
		ChartDialog dialog = new ModelessChartDialog(view, cachedShell,
				cachedName, selectedChart, originalSize) ;
		dialog.open();


	}

}

class ModelessChartDialog extends ChartDialog {
	
	private String title;
	
	private PlotView view;
	
	public ModelessChartDialog(PlotView view, Shell parentShell, String title, IChart chart,
			Point initialSize) {
		super(parentShell, chart, initialSize);
		setShellStyle(SWT.SHELL_TRIM | SWT.MAX | SWT.RESIZE | SWT.MODELESS);
		this.title = title;
		this.view = view;
	}
	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
	}
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent,
				IDialogConstants.OK_ID
						+ IDialogConstants.CANCEL_ID, "Attach",
				false);
		button.setToolTipText("Attach graph to Graph View");
		button.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				view.reveal(getChart(), title);
				okPressed();
			}

		});

		createButton(parent, IDialogConstants.OK_ID, "Close",
				true);
	}
}
