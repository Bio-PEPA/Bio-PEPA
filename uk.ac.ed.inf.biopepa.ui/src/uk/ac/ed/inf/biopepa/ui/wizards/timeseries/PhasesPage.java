package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import java.util.LinkedList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import uk.ac.ed.inf.biopepa.core.sba.ExperimentSet;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentSetReader;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentLine;
import uk.ac.ed.inf.biopepa.core.sba.PhaseLine;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;

public class PhasesPage extends WizardPage {
	public final static String wizardPageName = "Setup Phases";

	private BioPEPAModel model;
	private PhaseLine[] phaseLines;
	// private String loadedFileName;

	private Shell shell;
	
	public PhasesPage(BioPEPAModel model) {
		super(wizardPageName);
		this.model = model;
		this.shell = new Shell();
		// this.experimentSet = null ;
		setTitle("Import phases setup from cvs");
		setDescription("Set up phases from csv");
	}

	private void clearLoaded(){
		// loadedFileName = "";
		phaseLines = null;
	}
	
	public PhaseLine[] getPhaseLines(){
		return phaseLines;
	}
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);
		// Layout
		GridLayout gl = new GridLayout();
		// gl.marginRight = gl.marginRight ; // + imageData.width;
		composite.setLayout(gl);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.RIGHT;
		// gridData.horizontalIndent = 10;
		gridData.minimumWidth = SWT.DEFAULT;

		Button openCsvButton = new Button(composite, SWT.PUSH);
		openCsvButton.setText("Open a csv file");

		class Open implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Open");
				fd.setFilterPath("C:/");
				String[] filterExt = { "*.csv" };
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				// I should set a widget to display the selected
				// filename or something, maybe even display the
				// parsed contents
				readCSVFile(selected);
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}

		/*
		 * SelectionAdapter buttonListener = new SelectionAdapter(){ public void
		 * widgetSelected(SelectionEvent event){
		 * System.out.println("Yup, I already been clicked"); readCSVFile
		 * ("Not got no name"); } };
		 */
		openCsvButton.addSelectionListener(new Open());
		this.setMessage("No csv file has been loaded");

	}
	
	/*
	 * We attempt to read in the file as csv file 
	 * if we fail then we leave/set
	 * the experiment set to null, 
	 * thus meaning we must check for that.
	 */
	private void readCSVFile(String filename) {
		if (filename == null) {
			clearLoaded();
			System.out.println("boo hoo");
			MessageDialog.openError(shell, "Error importing csv", 
					"filename is null");
			return;
		}
		ExperimentSetReader esetReader = 
			new ExperimentSetReader (model.getSBAModel(), 
				model.getCompiledModel());
		String [] specialNames = { "phase-delay" };
		esetReader.readCsvFile(filename, specialNames);
		String readingError = esetReader.getReadError();
		ExperimentSet experSet = esetReader.getExperimentSet();
		if (experSet != null){
			loadExperimentSet (experSet);
			// this.loadedFileName = filename;
			this.setMessage("The csv file: " + filename + " has been loaded");
			// this.updateLoadedTable();
			return;
		} else {
			MessageDialog.openError(shell, "Error importing csv", readingError);
			clearLoaded();
			return;
		}
	}
	private void loadExperimentSet (ExperimentSet experSet){
		LinkedList<ExperimentLine> elines = experSet.getExperimentLines();
		int noLines = elines.size();
		this.phaseLines = new PhaseLine[noLines];
		for (int index = 0; index < noLines; index++){
			ExperimentLine el = elines.get(index);
			Number thisDelay = el.getSpecialDefine("phase-delay");
			if (thisDelay == null){
				MessageDialog.openError(shell, "No delay",
						"a phase line without a phase delay has been entered");
			}
			PhaseLine phaseLine = new PhaseLine (el, thisDelay.doubleValue());
		}
	}

}
