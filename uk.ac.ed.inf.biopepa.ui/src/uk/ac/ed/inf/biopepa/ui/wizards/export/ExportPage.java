/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.wizards.export;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.interfaces.Exporter;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.export.Exporters;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;

public class ExportPage extends WizardPage {
	
	public static final String name = "";
	
	private String optionsTitle = "Export options for ";
	
	Exporter exporter = null;
	
	Group description;
	
	Label descriptiveText;
	
	BioPEPAModel model;

	protected ExportPage(BioPEPAModel model) {
		super(name);
		setTitle("BioPEPA Export Wizard");
		setDescription("Please select the format you would like to export the Bio-PEPA file to.");
		this.model  = model;
		setPageComplete(true);
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
	
		final Combo exportCombo = new Combo(composite, SWT.READ_ONLY);
		description = new Group(composite, SWT.NONE);
		description.setText("Description");
		descriptiveText = new Label(description, SWT.WRAP);
		Group exportOptions = new Group(composite, SWT.NONE);
		exportOptions.setText(optionsTitle);
		final String[] choices = Exporters.getShortNames();
		exportCombo.setItems(choices);
		exportCombo.select(0);
		exporter = Exporters.getSolverInstance(choices[0]);
		checkPage();
		exportCombo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int i = exportCombo.getSelectionIndex();
				if(i == -1)
					return;
				exporter = Exporters.getSolverInstance(choices[i]);
				checkPage();
			}
		});
		composite.setLayout(new FormLayout());
		description.setLayout(new FormLayout());
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		exportCombo.setLayoutData(formData);
		
		formData = new FormData();
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.top = new FormAttachment(exportCombo);
		description.setLayoutData(formData);
		
		formData = new FormData();
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.top = new FormAttachment(0);
		descriptiveText.setLayoutData(formData);
		
		formData = new FormData();
		formData.top = new FormAttachment(description);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		exportOptions.setLayoutData(formData);
		
		Composite exportParent = new Composite(exportOptions, SWT.NONE);
		exportParent.setLayout(new GridLayout(2, false));
		
		setControl(composite);
	}
	
	public void checkPage() {
		setPageComplete(false);
		if(exporter == null) {
			setErrorMessage("Exporter not found. Please try selecting another.");
			return;
		}
		String s = exporter.getDescription();
		if(s != null && s != "") {
			descriptiveText.setText(s);
			description.setVisible(true);
		} else {
			description.setVisible(false);
		}
		Object o = exporter.requiredDataStructure();
		if(o.equals(ModelCompiler.class))
			exporter.setModel(model.getCompiledModel());
		else if(o.equals(SBAModel.class))
			exporter.setModel(model.getSBAModel());
		else {
			setErrorMessage("Cannot supply model in an acceptable form to use this exporter. Please try selecting another.");
			return;
		}
		String response = exporter.canExport();
		if(response != null) {
			setErrorMessage("Cannot use exporter. " + response);
			return;
		}
		setErrorMessage(null);
		setPageComplete(true);

	}
	
	public Exporter getExporter() {return exporter;}
}
