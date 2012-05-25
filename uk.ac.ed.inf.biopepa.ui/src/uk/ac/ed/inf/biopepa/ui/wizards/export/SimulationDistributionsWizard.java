package uk.ac.ed.inf.biopepa.ui.wizards.export;

import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SaveAsDialog;

import uk.ac.ed.inf.biopepa.core.sba.FileStringConsumer;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.export.BioNessieTraceLog;
import uk.ac.ed.inf.biopepa.core.sba.export.ManyTraceLogger;
import uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer;
import uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer.NullTraceLog;
import uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer.SimulationTraceLog;
import uk.ac.ed.inf.biopepa.core.sba.export.TraviandoExport.TraviandoTraceLog;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;
import uk.ac.ed.inf.biopepa.ui.interfaces.IResourceProvider;
import uk.ac.ed.inf.biopepa.ui.wizards.timeseries.PhasesPage;

public class SimulationDistributionsWizard extends Wizard implements IResourceProvider {

	BioPEPAModel model;
	
	private ExportPage exportPage;
	private PhasesPage phasesPage;
	
	public SimulationDistributionsWizard(BioPEPAModel model) {
		if(model == null)
			throw new NullPointerException("Error; model does not exist.");
		this.model = model;
		setHelpAvailable(false);
		setWindowTitle("Export options for Bio-PEPA");
	}
	
	
	private class ExportPage extends WizardPage {
		
		private Combo targetNameCombo;
		private Text targetValueText;
		private Text stopTimeText;
		private Text dataPointsText;
		private Text numberRunsText;

		protected ExportPage(String pageName) {
			super(pageName);
			this.setTitle("Simulation Distributions");
			this.setDescription("Use simulation runs to calculate the probability " +
					            "for a species to reach a particular population " +
					            "size by each time point");
		}
		
		private int textStyle = SWT.RIGHT | SWT.BORDER;
		private int labelStyle = SWT.SINGLE | SWT.LEFT;

		private String[] combineStringArrays(String[] l, String[] r){
			String[] result = new String[l.length + r.length];
			for (int index = 0; index < l.length; index++){
				result[index] = l[index];
			}
			for (int index = 0; index < r.length; index++){
				result[index + l.length] = r[index];
			}
			return result;
		}
		
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			
			setControl(composite);
			
			// Create an inner composite for the labels and text fields
			Composite labelsComposite = new Composite(composite, SWT.NONE);
			GridLayout labelsCompLayout = new GridLayout(2, false);
			labelsComposite.setLayout(labelsCompLayout);		
			
			
			Label targetNameLabel = new Label(labelsComposite, labelStyle);
			targetNameLabel.setText ("Choose the dynamic component: ");
			targetNameCombo = new Combo(labelsComposite, SWT.READ_ONLY);
			String[] varNames = model.getDynamicVariableNames();
			String[] compNames = model.getComponentNames();
			String[] choices = combineStringArrays(varNames, compNames);
			targetNameCombo.setItems(choices);
			targetNameCombo.select(0);
			
			Label targetValueLabel = new Label(labelsComposite, labelStyle);
			targetValueLabel.setText ("Enter target value");
			targetValueText = new Text (labelsComposite, textStyle);
			targetValueText.setText (Integer.toString(this.defaultTargetValue));
						
			Label stopTimeLabel = new Label(labelsComposite, labelStyle);
			stopTimeLabel.setText("Set the stop time");
			stopTimeText = new Text(labelsComposite, textStyle);
			stopTimeText.setText(Double.toString(this.defaultStopTime));
			stopTimeText.setLayoutData(newTextGridData());
			stopTimeText.addModifyListener(modifyListener);
			
			Label explainNumberRuns = new Label (labelsComposite, labelStyle);
			explainNumberRuns.setText("The number of independent runs");
			
			numberRunsText = new Text (labelsComposite, textStyle);
			numberRunsText.setText(Integer.toString(this.defaultNumberRuns));
			numberRunsText.setLayoutData(newTextGridData());
			numberRunsText.addModifyListener(modifyListener);
			
			Label explainDataPoints = new Label (labelsComposite, labelStyle);
			explainDataPoints.setText(
					"Set the increment in data point size for the graph");
			// explainDataPoints.setEnabled(false);
			
			dataPointsText = new Text (labelsComposite, textStyle);
			dataPointsText.setText(Double.toString(this.defaultDataPointSize));
			dataPointsText.setLayoutData(newTextGridData());
			dataPointsText.addModifyListener(modifyListener);
			
			enableWidgets();
			validate();
		}
		
		/*
		 * A simple method to create a grid data object for
		 * text object, since I think it is better to create a
		 * new one for each text object such that any changes are
		 * not then global.
		 */
		private GridData newTextGridData (){
			GridData textGridData = new GridData ();
			textGridData.widthHint = 80;
			textGridData.horizontalAlignment = GridData.FILL;
			textGridData.grabExcessHorizontalSpace = true;
			return textGridData;
		}
		
		private ModifyListener modifyListener = new ModifyListener (){

			public void modifyText(ModifyEvent arg0) {
				validate ();
			}
			
		};

		private void enableWidgets(){
			/*
			if(displayGraph.getSelection()){
				dataPointsText.setEnabled(true);
			} else {
				dataPointsText.setEnabled(false);
			}
			*/
		}

		private boolean isValidInt(Text textBox){
			String text = textBox.getText().trim();
			try {
				Integer.parseInt(text);
			} catch (Exception e){
				return false;
			}
			return true;
		}
		
		private boolean isValidDouble(Text textBox){
			String text = textBox.getText().trim();
			try {
				Double.parseDouble(text);
			} catch (Exception e){
				return false;
			}
			return true;
		}
		
		private void validate() {
			/*
			 * I would actually prefer to set the page to
			 * complete and then only set it to false if we
			 * actually encounter an error. That way we could
			 * report all errors. Unfortunately you can currently
			 * only report one error. It doesn't really matter since
			 * this gets called whenever the user makes a change
			 * (eg. when fixing the current error) so they will always
			 * be getting feedback.
			 */
			this.setPageComplete(false);
			this.setErrorMessage(null);
			
			// TODO: reorder these so that they are in the same
			// order as they appear on the page so that the user
			// can fix error in an intuitive order.
			
			if (!isValidDouble(dataPointsText)){
				this.setPageComplete(false);
				this.setErrorMessage("Cannot parse data points");
				return;
			}
			if (!isValidInt(targetValueText)){
				this.setPageComplete(false);
				this.setErrorMessage ("Cannot parse target value");
				return ;
			}
			
			if (!isValidDouble(stopTimeText)){
				this.setPageComplete(false);
				this.setErrorMessage ("Cannot parse stop time");
				return ;
			}
			
			if (!isValidInt(numberRunsText)){
				this.setPageComplete(false);
				this.setErrorMessage ("Cannot parse number of runs");
				return ;
			}
				
			// If we get this far without returning then the page
			// must be complete.
			this.setPageComplete(true);
		}

		
		public String getTargetIdentifier (){
			int index = targetNameCombo.getSelectionIndex();
			String[] items = targetNameCombo.getItems();
			return items[index];
		}
		private int defaultTargetValue = 20;
		public int getTargetValue (){
			String text = targetValueText.getText().trim();
			try {
				return Integer.parseInt(text);
			} catch (Exception e) {
				return this.defaultTargetValue;
			}
		}
		
		private double defaultStopTime = 20.0;
		public double getStopTime() {
			String text = stopTimeText.getText().trim();
			try {
				return Double.parseDouble(text);
			} catch (Exception e) {
				return 0;
			}
		}

		private int defaultNumberRuns = 100;
		public int getNumberRuns (){
			String runsText = numberRunsText.getText().trim();
			if (!runsText.isEmpty()){
				try{
					return Integer.parseInt(runsText);
				} catch (Exception e){
					return this.defaultNumberRuns;
				}
			} else {
				return this.defaultNumberRuns;
			}
		}
		
		/*
		public boolean getDisplayGraph(){
			return displayGraph.getSelection();
		}
		*/
		private double defaultDataPointSize = 0.1;
		public double getDataPointSize() {
				String text = dataPointsText.getText().trim();
				if (text.isEmpty()) {
					return this.defaultDataPointSize;
				} else {
					try {
						return Double.parseDouble(text);
					} catch (Exception e) {
						return this.defaultDataPointSize;
					}
				}
		}
	}
	
	public void addPages (){
		exportPage = new ExportPage("Export a Traviando Trace");
		addPage (exportPage);
		/*phasesPage = new PhasesPage (model);
		addPage (phasesPage);*/
	}
	
	@Override
	public boolean performFinish() {
		SBAModel sbaModel = model.getSBAModel();
		IPath modelPath  = model.getUnderlyingResource().getFullPath();
		String modelName = modelPath.removeFileExtension().lastSegment();
		SimulationTracer simTrace = new SimulationTracer(sbaModel);
		SimulationsDistributionJob simJob = 
			new SimulationsDistributionJob(modelName);
		
		simJob.setTargetComp(exportPage.getTargetIdentifier());
		simJob.setTargetValue(exportPage.getTargetValue());
		simJob.setReplications(exportPage.getNumberRuns());
		
		simTrace.setDataPointStep(exportPage.getDataPointSize());
		// simTrace.setFiringsLimit(exportPage.getFiringsLimit());
		simTrace.setTimeLimit(exportPage.getStopTime());

		// Now we set up any defined phases, if these aren't
		// defined then they should be null and the
		// simulation trace engine will take care of that and
		// create one large phase lasting the entire simulation.
		// simTrace.setDelays(phasesPage.getDelays());
		// simTrace.setPhaseLines(phasesPage.getPhaseLines());

		// simJob.setDoGraph(exportPage.getDisplayGraph());
		simJob.setSimulationTracer(simTrace);
	
		simJob.schedule();
		return true;
	}

	public IResource getUnderlyingResource() {
		return model.getUnderlyingResource();
	}
}
