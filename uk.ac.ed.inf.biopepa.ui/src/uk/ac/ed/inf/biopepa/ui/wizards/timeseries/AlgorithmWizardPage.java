/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import java.util.*;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import uk.ac.ed.inf.biopepa.core.interfaces.Solver;
import uk.ac.ed.inf.biopepa.core.interfaces.Solver.SolverResponse;
import uk.ac.ed.inf.biopepa.core.interfaces.Solver.SolverResponse.Suitability;
import uk.ac.ed.inf.biopepa.core.sba.Parameters;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.Solvers;
import uk.ac.ed.inf.biopepa.core.sba.Parameters.Parameter;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;
/**
 * 
 * @author ajduguid
 *
 */
public class AlgorithmWizardPage extends WizardPage {

	public static final String name = "Algorithm";

	Listener controlListener;

	HashMap<Control, Parameter> controlMap;

	boolean firstUse = true;

	HashSet<Parameter> invalidParameters;

	BioPEPAModel model;

	Group optionsGroup;

	Combo solverCombo;

	Composite solverComposite;
	
	Solver solver;
	
	Map<Parameter, Object> uParameters;
	
	Parameters parameters;
	
	protected AlgorithmWizardPage(BioPEPAModel model, Map<Parameter, Object> parameters) {
		super(name);
		this.model = model;
		controlMap = new HashMap<Control, Parameter>();
		invalidParameters = new HashSet<Parameter>();
		setTitle("Solver selection and Parameter input");
		uParameters = parameters;
		setPageComplete(solver != null);
	}

	public void algorithmChanged() {
		controlMap.clear();
		invalidParameters.clear();
		for (Control child : optionsGroup.getChildren())
			if (!child.isDisposed())
				child.dispose();
		// Save all values
		/*
		if(parameters != null) {
			for(Parameter p : parameters.arrayOfKeys())
				uParameters.put(p, parameters.getValue(p));
		}*/
		solver = Solvers.getSolverInstance(solverCombo.getItem(solverCombo.getSelectionIndex()));
		((TimeSeriesAnalysisWizard) getWizard()).solver = solver;
		SolverResponse sr = solver.getResponse(model.getSBAModel());
		if(sr.getSuitability().equals(Suitability.WARNING)) {
			setMessage(sr.getMessage(), INFORMATION);
		} else {
			setMessage(null, NONE);
			setDescription("Parameters required for the " + solver.getDescriptiveName());
		}
		parameters = solver.getRequiredParameters();
		((TimeSeriesAnalysisWizard) getWizard()).parameters = parameters;
		Label label;
		Control control;
		String s;
		for (Parameter parameter : parameters.arrayOfKeys()) {
			// ignore these parameters (taken care of elsewhere)
			if (parameter.equals(Parameter.Components))
				continue;
			label = new Label(optionsGroup, SWT.LEFT);
			label.setText(parameter.toString());
			Class<?> c = parameter.getType();
			if (Number.class.isAssignableFrom(c)) {
				control = new Text(optionsGroup, SWT.RIGHT | SWT.SINGLE);
				if(uParameters.containsKey(parameter)) {
					s = uParameters.get(parameter).toString();
					parameters.setValue(parameter, uParameters.get(parameter));
				}
				else
					s = parameter.getDefault().toString();
				((Text) control).setText(s);
				control.addListener(SWT.Modify, controlListener);
				control.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				controlMap.put(control, parameter);
			}
		}
		optionsGroup.layout();
		solverComposite.layout();
		validatePage();
	}

	public void createControl(Composite parent) {
		solverComposite = new Composite(parent, SWT.NONE);
		solverComposite.setLayout(new FormLayout());
		solverCombo = new Combo(solverComposite, SWT.READ_ONLY);
		String[] sArray = Solvers.getSolverList();
		ArrayList<String> tArrayList = new ArrayList<String>();
		SBAModel sbaModel = model.getSBAModel();
		Solver solver;
		Map<String, Set<String>> disallowed = new HashMap<String, Set<String>>();
		String message;
		Set<String> tSet;
		for(String s : sArray) {
			solver = Solvers.getSolverInstance(s);
			SolverResponse sr = solver.getResponse(sbaModel);
			if(!sr.getSuitability().equals(Suitability.UNSUITABLE))
				tArrayList.add(s);
			else {
				message = sr.getMessage();
				if(disallowed.containsKey(message))
					disallowed.get(message).add(s);
				else {
					tSet = new HashSet<String>();
					tSet.add(s);
					disallowed.put(message, tSet);
				}
			}
		}
		sArray = tArrayList.toArray(new String[] {});
		solverCombo.setItems(sArray);
		solver = ((TimeSeriesAnalysisWizard) getWizard()).solver;
		if(solver != null) {
			for(int i = 0; i < sArray.length; i++)
				if(sArray[i].equals(solver.getDescriptiveName())) {
					solverCombo.select(i);
					break;
				}
		} else
			solverCombo.select(0);				
		solverCombo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (solverCombo.getSelectionIndex() != -1)
					algorithmChanged();
			}
		});
		optionsGroup = new Group(solverComposite, SWT.NONE);
		optionsGroup.setText("Solver Parameters");
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		optionsGroup.setLayout(layout);
		controlListener = new Listener() {
			public void handleEvent(Event event) {
				Control control = (Control) event.widget;
				Parameter parameter = controlMap.get(control);
				try {
					if (control instanceof Text) {
						parameters.setValue(parameter, ((Text) control).getText());
						uParameters.put(parameter, parameters.getValue(parameter));
					}
					// no error thrown so valid type
					invalidParameters.remove(parameter);
				} catch (IllegalArgumentException e) {
					invalidParameters.add(parameter);
				}
				validatePage();
			}
		};
		Label text = new Label(solverComposite, SWT.WRAP);
		if(!disallowed.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			String term = System.getProperty("line.separator");
			sb.append("Certain solvers are not available due to certain features used within the model.");
			sb.append(" The following are not available:").append(term).append(term);
			for(String s : disallowed.keySet()) {
				sb.append("Reason : ").append(s).append(term);
				tSet = disallowed.get(s);
				if(tSet.size() > 1) {
					sb.append("Solvers :").append(term);
					for(String s2 : tSet)
						sb.append("\t\t").append(s2).append(term);
				} else
					sb.append("Solver : ").append(tSet.iterator().next()).append(term);
			}
			sb.delete(sb.length() - term.length(), sb.length());
			text.setText(sb.toString());
		}
		// setImageDescriptor(ImageDescriptor.createFromImage(JFaceResources.getImage(org.eclipse.jface.dialogs.Dialog.DLG_IMG_MESSAGE_INFO)));
		setMessage("test message", INFORMATION);
		// Layout composites
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		solverCombo.setLayoutData(formData);
		formData = new FormData();
		formData.top = new FormAttachment(solverCombo);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		optionsGroup.setLayoutData(formData);
		formData = new FormData();
		formData.top = new FormAttachment(optionsGroup);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		text.setLayoutData(formData);
		setControl(solverComposite);
		algorithmChanged();
	}

	private void validatePage() {
		if (invalidParameters.isEmpty()) {
			setPageComplete(true);
			setErrorMessage(null);
		} else { // ergo there is at least one Parameter in the set
			setPageComplete(false);
			setErrorMessage("Invalid value entered for "
					+ invalidParameters.iterator().next().toString());
		}
	}
}
