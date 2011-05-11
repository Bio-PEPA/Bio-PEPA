/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.wizards.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.ed.inf.common.launching.ILaunchingConstants;
import uk.ac.ed.inf.common.ui.wizards.IActionFieldsProvider;
import uk.ac.ed.inf.common.ui.wizards.PassageTimeAnalysisWizard;

/**
 * Page with passage-time options.
 * 
 * @author mtribast
 * 
 */
public class PassageTimePage extends WizardPage implements IUpdatable {

	private static final Map<String, String> SOLVER_NAMES = new HashMap<String, String>();

	private static final Map<String, String> ANALYSIS_TYPES = new HashMap<String, String>();

	static {
		SOLVER_NAMES.put("Gauss", ILaunchingConstants.IPC_GAUSS);
		SOLVER_NAMES.put("Grassman", ILaunchingConstants.IPC_GRASSMAN);
		SOLVER_NAMES.put("Gauss-Seidel", ILaunchingConstants.IPC_GAUSS_SEIDEL);
		SOLVER_NAMES.put("Successive Overrelaxation",
				ILaunchingConstants.IPC_SOR);
		SOLVER_NAMES.put("BiConjugate Gradient", ILaunchingConstants.IPC_BICG);
		SOLVER_NAMES.put("BiConjugate Gradient Stabilised",
				ILaunchingConstants.IPC_BICGSTAB);
		SOLVER_NAMES.put("BiConjugate Gradient Stabilised 2",
				ILaunchingConstants.IPC_BICGSTAB2);
		SOLVER_NAMES.put("Conjugate Gradient on Normal Equations",
				ILaunchingConstants.IPC_CGNR);
		SOLVER_NAMES.put("Conjugate Gradient Stabilised",
				ILaunchingConstants.IPC_CGS);
		SOLVER_NAMES.put("Transpose-Free Quasi-Minimum Residual",
				ILaunchingConstants.IPC_TFQMR);
		SOLVER_NAMES.put("Aggregation Isolation", ILaunchingConstants.IPC_AI);
		SOLVER_NAMES.put("AIR with table-driven relaxation",
				ILaunchingConstants.IPC_AIR);
		SOLVER_NAMES.put("Automatic", ILaunchingConstants.IPC_AUTOMATIC);

		ANALYSIS_TYPES
				.put("Passage Time", ILaunchingConstants.IPC_PASSAGE_TIME);
	}

	private Combo solver = null;

	private Text sourceActions = null;

	private Text targetActions = null;

	private Text startTime = null;

	private Text stopTime = null;

	private Text timeStep = null;

	private Text advancedText = null;

	private Map<String, String> optionMap;

	private IActionFieldsProvider fProvider;

	private Button staticAnalysisButton;

	private Button useLocationAwareButton;

	private Text locationAwareComponent;

	public PassageTimePage(String pageName, IActionFieldsProvider provider) {
		super(pageName);
		this.fProvider = provider;
		setTitle("Analysis Settings");
		setDescription("Configure ipc/hydra for this analysis.");
	}

	public void setWizard(IWizard wizard) {
		super.setWizard(wizard);
		optionMap = ((PassageTimeAnalysisWizard) wizard).getOptionMap();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.inf.srmc.ui.wizards.IUpdatable#update()
	 */
	public void update() {
		// TODO Auto-generated method stub
		if (!isControlCreated())
			return;
		String solverCode = null;
		for (Map.Entry<String, String> entry : SOLVER_NAMES.entrySet()) {
			if (entry.getKey().equals(solver.getText())) {
				solverCode = entry.getValue();
				break;
			}
		}
		optionMap.put(ILaunchingConstants.IPC_SOLVER_NAME_KEY, solverCode);
		optionMap.put(ILaunchingConstants.IPC_SOURCE_ACTIONS, sourceActions
				.getText());
		optionMap.put(ILaunchingConstants.IPC_TARGET_ACTIONS, targetActions
				.getText());
		optionMap.put(ILaunchingConstants.IPC_START_TIME, startTime.getText());
		optionMap.put(ILaunchingConstants.IPC_STOP_TIME, stopTime.getText());
		optionMap.put(ILaunchingConstants.IPC_TIME_STEP, timeStep.getText());
		optionMap.put(ILaunchingConstants.IPC_ADVANCED_ARGUMENTS, advancedText
				.getText());
		String value = Boolean.toString(staticAnalysisButton.getSelection());
		optionMap.put(ILaunchingConstants.IPC_STATIC_ANALYSIS, value);

		boolean useLocationAware = useLocationAwareButton.getSelection();
		String locAware = Boolean.toString(useLocationAware);
		optionMap.put(ILaunchingConstants.IPC_USE_LOCATION_AWARE_PROBE,
				locAware);
		if (useLocationAware) {
			optionMap.put(ILaunchingConstants.IPC_PROBE_COMPONENT,
					locationAwareComponent.getText());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout solverLayout = new GridLayout();
		solverLayout.numColumns = 2;
		solverLayout.makeColumnsEqualWidth = false;
		main.setLayout(solverLayout);
		setControl(main);

		ModifyListener listener = new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updatePage();
			}

		};

		SelectionListener sListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updatePage();
			}
		};

		/*
		 * SOLVERS
		 */
		Label solverLabel = new Label(main, SWT.NULL);
		solverLabel
				.setText("Solver for steady-state probability distribution:");
		solverLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		solver = new Combo(main, SWT.READ_ONLY);
		solver.addSelectionListener(sListener);
		for (String name : SOLVER_NAMES.keySet())
			solver.add(name);

		solver.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/*
		 * ANALYSIS
		 */
		/*
		 * Label analysisLabel = new Label(main, SWT.NULL);
		 * analysisLabel.setText("Analysis Type:");
		 * 
		 * analysis = new Combo(main, SWT.READ_ONLY);
		 * analysis.addSelectionListener(sListener); for (String type :
		 * ANALYSIS_TYPES.keySet()) analysis.add(type);
		 * analysis.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		 */

		/*
		 * SOURCE ACTIONS
		 */
		Label sourceLabel = new Label(main, SWT.NULL);
		sourceLabel.setText("Source Actions (comma-separated list):");

		sourceActions = new Text(main, SWT.BORDER);
		sourceActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sourceActions.addModifyListener(listener);
		/*
		 * TARGET ACTIONS
		 */
		Label targetLabel = new Label(main, SWT.NULL);
		targetLabel.setText("Target Actions (comma-separated list):");

		targetActions = new Text(main, SWT.BORDER);
		targetActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		targetActions.addModifyListener(listener);
		/*
		 * START TIME
		 */
		Composite startComp = new Composite(main, SWT.NULL);
		startComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout startLayout = new GridLayout(2, false);
		startComp.setLayout(startLayout);
		Label startLabel = new Label(startComp, SWT.NULL);
		startLabel.setText("Start Time:");
		startTime = new Text(startComp, SWT.BORDER);
		startTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		startTime.addModifyListener(listener);
		/*
		 * STOP TIME
		 */
		Composite stopComp = new Composite(main, SWT.NULL);
		stopComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout stopLayout = new GridLayout(2, false);
		stopComp.setLayout(stopLayout);
		Label stopLabel = new Label(stopComp, SWT.NULL);
		stopLabel.setText("Stop Time:");
		stopTime = new Text(stopComp, SWT.BORDER);
		stopTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		stopTime.addModifyListener(listener);
		/*
		 * TIME STEP
		 */
		Composite stepComp = new Composite(main, SWT.NULL);
		GridData stepData = new GridData(GridData.FILL_HORIZONTAL);
		stepData.horizontalSpan = 2;
		stepComp.setLayoutData(stepData);
		GridLayout stepLayout = new GridLayout(2, false);
		stepComp.setLayout(stepLayout);
		Label stepLabel = new Label(stepComp, SWT.NULL);
		stepLabel.setText("Time Step:");
		timeStep = new Text(stepComp, SWT.BORDER);
		timeStep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		timeStep.addModifyListener(listener);
		/*
		 * ADVANCED GROUP
		 */
		Group advancedGroup = new Group(main, SWT.SHADOW_OUT);
		advancedGroup.setText("Advanced Options");
		GridData advData = new GridData(GridData.FILL_BOTH);
		advData.horizontalSpan = 2;
		advancedGroup.setLayoutData(advData);
		advancedGroup.setLayout(new GridLayout());

		staticAnalysisButton = new Button(advancedGroup, SWT.CHECK);
		staticAnalysisButton.setText("Perform static analysis");
		staticAnalysisButton.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		useLocationAwareButton = new Button(advancedGroup, SWT.CHECK);
		useLocationAwareButton.setText("Use location-aware probe");
		useLocationAwareButton.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		locationAwareComponent = new Text(advancedGroup, SWT.BORDER);
		GridData gData = new GridData(
				GridData.FILL_HORIZONTAL);
		gData.horizontalIndent = 20;
		locationAwareComponent.setLayoutData(gData);

		Label extraOptions = new Label(advancedGroup, SWT.NULL);
		extraOptions.setText("Additional Options");
		extraOptions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		advancedText = new Text(advancedGroup, SWT.BORDER | SWT.MULTI
				| SWT.WRAP);
		advancedText.setLayoutData(new GridData(GridData.FILL_BOTH));
		advancedText.addModifyListener(listener);

		useLocationAwareButton.addSelectionListener(sListener);
		useLocationAwareButton.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent e) {
				locationAwareComponent.setFocus();
			}
		});
		initialisePage();

	}

	private void initialisePage() {

		/* SOLVER */
		String solverCode = (String) optionMap
				.get(ILaunchingConstants.IPC_SOLVER_NAME_KEY);
		if (solverCode == null)
			solverCode = ILaunchingConstants.DEFAULT_SOLVER;
		for (Map.Entry<String, String> entry : SOLVER_NAMES.entrySet()) {
			if (entry.getValue().equals(solverCode)) {
				solver.setText(entry.getKey());
				break;
			}
		}

		/* SOURCE */
		String sourceAction = (String) optionMap
				.get(ILaunchingConstants.IPC_SOURCE_ACTIONS);
		if (sourceAction == null)
			sourceAction = ILaunchingConstants.DEFAULT_SOURCE;
		sourceActions.setText(sourceAction);

		/* TARGET */
		String targetAction = (String) optionMap
				.get(ILaunchingConstants.IPC_TARGET_ACTIONS);
		if (targetAction == null)
			targetAction = ILaunchingConstants.DEFAULT_TARGET;
		targetActions.setText(targetAction);

		/* START TIME */
		String start = (String) optionMap
				.get(ILaunchingConstants.IPC_START_TIME);
		if (start == null)
			start = ILaunchingConstants.DEFAULT_START_TIME;
		startTime.setText(start);

		/* STOP TIME */
		String stop = (String) optionMap.get(ILaunchingConstants.IPC_STOP_TIME);
		if (stop == null)
			stop = ILaunchingConstants.DEFAULT_STOP_TIME;
		stopTime.setText(stop);

		/* STEP */
		String step = (String) optionMap.get(ILaunchingConstants.IPC_TIME_STEP);
		if (step == null)
			step = ILaunchingConstants.DEFAULT_TIME_STEP;
		timeStep.setText(step);

		/* PERFORM STATIC ANALYSIS */
		String staticAnalysis = (String) optionMap
				.get(ILaunchingConstants.IPC_STATIC_ANALYSIS);
		if (staticAnalysis == null)
			staticAnalysis = ILaunchingConstants.DEFAULT_STATIC_ANALYSIS;
		staticAnalysisButton.setSelection(Boolean.parseBoolean(staticAnalysis));

		/* LOCATION-AWARE PROBES */
		String locationAware = (String) optionMap
				.get(ILaunchingConstants.IPC_USE_LOCATION_AWARE_PROBE);
		if (locationAware == null)
			locationAware = ILaunchingConstants.DEFAULT_LOCATION_AWARE;
		useLocationAwareButton
				.setSelection(Boolean.parseBoolean(locationAware));

		String locAwareComponent = (String) optionMap
				.get(ILaunchingConstants.IPC_PROBE_COMPONENT);
		if (locAwareComponent == null)
			locAwareComponent = "";
		locationAwareComponent.setText(locAwareComponent);

		String adv = (String) optionMap
				.get(ILaunchingConstants.IPC_ADVANCED_ARGUMENTS);
		if (adv == null) {
			adv = "";
		}
		advancedText.setText(adv);

		updatePage();

		// notifies provider
		fProvider.setSourceActionControl(sourceActions);
		fProvider.setTargetActionControl(targetActions);
		fProvider.setComponentNameControl(locationAwareComponent);

	}

	private void updatePage() {

		String message = validate();
		setErrorMessage(message);
		if (message == null) {
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
	}

	private String validate() {
		setMessage(null);
		
		locationAwareComponent.setEnabled(useLocationAwareButton
				.getSelection());

		if (sourceActions.getText().trim().equals("")) {
			return "At least one source action needs to be specified";

		}

		if (targetActions.getText().trim().equals("")) {
			return "At least one target action needs to be specified";

		}
		double start, stop;

		try {
			start = Double.parseDouble(startTime.getText().trim());
			if (start <= 0.0) {
				return "Start time must be positive";
			}

		} catch (NumberFormatException nfe) {
			return "Start time is not a valid double";

		}

		try {
			stop = Double.parseDouble(stopTime.getText().trim());
			if (stop <= 0.0) {
				return "Stop time must be positive";
			}

		} catch (NumberFormatException nfe) {
			return "Stop time is not a valid double";
		}
		if (start >= stop)
			return "Invalid time interval";

		try {
			double step = Double.parseDouble(timeStep.getText().trim());
			if (step < 0.0) {
				return "Time step cannot be negative";
			}

		} catch (NumberFormatException nfe) {
			return "Time  step is not a valid double";
		}
		return null;
	}

}
