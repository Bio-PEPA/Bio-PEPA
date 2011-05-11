/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/

package uk.ac.ed.inf.common.ui.plotview.views.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author mtribast
 * 
 */
public class SaveChartDialog extends TitleAreaDialog {

	private Point originalValues = null;
	
	private Point result = null;

	private Text widthText;

	private Text heightText;
	
	public SaveChartDialog(Shell parentShell, int width, int height) {
		super(parentShell);
		originalValues = new Point(width, height);
		result = new Point(width, height);
	}
	
	/**
	 * Returns the selected size, or null if the dialog is canceled
	 * @return
	 */
	public Point getPoint() {
		return result;
	}
	
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle("Export Options");
		setMessage("Select size and resolution");
		return contents;

	}

	protected Control createDialogArea(Composite parent) {
		// top level composite
		Composite parentComposite = (Composite) super.createDialogArea(parent);
		Composite main = new Composite(parentComposite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		main.setLayout(layout);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setFont(parentComposite.getFont());

		Group group = new Group(main, SWT.NULL);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		group.setLayoutData(data);
		group.setText("Select Size");
		GridLayout groupLayout = GridLayoutFactory.copyLayout(layout);
		group.setLayout(groupLayout);
		ModifyListener listener = new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				setDialogComplete(validatePage());
			}
		};
		widthText = createRow(group, "Width", originalValues.x);
		widthText.addModifyListener(listener);
		heightText = createRow(group, "Height", originalValues.y);
		heightText.addModifyListener(listener);
		
		Button restoreOriginal = new Button(group, SWT.PUSH);
		restoreOriginal.setText("Reset");
		restoreOriginal.setToolTipText("Restore current size");
		restoreOriginal.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				widthText.setText(Integer.toString((originalValues.x)));
				heightText.setText(Integer.toString((originalValues.y)));
				setDialogComplete(validatePage());
			}
			
		});
		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 2;
		buttonData.horizontalAlignment = SWT.RIGHT;
		restoreOriginal.setLayoutData(buttonData);
		return parentComposite;
	}
	
	private void setDialogComplete(boolean complete) {
		getButton(IDialogConstants.OK_ID).setEnabled(complete);
	}
	
	private boolean validatePage() {
		setErrorMessage(null);
		result = null;
		int currentWidth = 0, currentHeight = 0;
		try {
			currentWidth =	Integer.parseInt(widthText.getText());
			if (currentWidth <=0 ) {
				setErrorMessage("Width must be a positive integer");
				return false;
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Invalid width");
			return false;
		}
		try {
			currentHeight =	Integer.parseInt(heightText.getText());
			if (currentHeight <=0 ) {
				setErrorMessage("Height must be a positive integer");
				return false;
			}
		} catch (NumberFormatException e) {
			setErrorMessage("Invalid height");
			return false;
		}
		result = new Point(currentWidth,currentHeight);
		return true;
	}
	
	protected void cancelPressed() {
		result = null;
		super.cancelPressed();
	}

	private Text createRow(Composite main, String label, int value) {
		Label rowLabel = new Label(main, SWT.NULL);
		rowLabel.setText(label);
		rowLabel.setLayoutData(new GridData());
		Text rowText = new Text(main, SWT.BORDER);
		rowText.setText(Integer.toString(value));
		rowText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return rowText;
	}

}
