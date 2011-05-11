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

public class TraviandoExportWizard extends Wizard implements IResourceProvider {

	BioPEPAModel model;
	
	private ExportPage exportPage;
	private PhasesPage phasesPage;
	
	public TraviandoExportWizard(BioPEPAModel model) {
		if(model == null)
			throw new NullPointerException("Error; model does not exist.");
		this.model = model;
		setHelpAvailable(false);
		setWindowTitle("Export options for Bio-PEPA");
	}
	
	
	private class ExportPage extends WizardPage {
		
		private Button firingsLimitCheck;
		private Text firingsLimitText;
		
		private FileSetter traviandoFileSetter;
		private FileSetter bioNessieFileSetter;
		private FileSetter sbrmlFileSetter;
		
		private Button timingLimitCheck;
		private Text timeLimitText;
		private Button displayCommentsButton;
		private Text modelCommentText;
		private Button displayGraph;
		private Text dataPointsText;
		private Text numberRunsText;

		protected ExportPage(String pageName) {
			super(pageName);
			this.setTitle("Simulation Trace");
			this.setDescription("Set up a Simulation trace with possible export");
		}
		
		private int textStyle = SWT.RIGHT | SWT.BORDER;
		private int labelStyle = SWT.SINGLE | SWT.LEFT;

		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			
			setControl(composite);

			/* Just a small label to say what to do */
			Label tmpLabel = new Label(composite, labelStyle);
			tmpLabel.setText("Please set the time or firings limit for this trace");
			tmpLabel.setLayoutData(createDefaultGridData());
			
			// Create an inner composite for the labels and text fields
			Composite labelsComposite = new Composite(composite, SWT.NONE);
			GridLayout labelsCompLayout = new GridLayout(3, false);
			labelsComposite.setLayout(labelsCompLayout);		
			
			
			Label firingLabel = new Label (labelsComposite, labelStyle);
			firingsLimitCheck = new Button (labelsComposite, SWT.CHECK);
			firingsLimitCheck.setSelection(false);
			firingsLimitCheck.addListener(SWT.Selection, checkBoxListener);
			firingLabel.setText("Number of firings limit");
			firingsLimitText = new Text(labelsComposite, textStyle);
			firingsLimitText.setText("2000");
			firingsLimitText.setLayoutData(newTextGridData());
			firingsLimitText.addModifyListener(modifyListener);
				
			Label timeLimitLabel = new Label (labelsComposite, labelStyle);
			timingLimitCheck = new Button (labelsComposite, SWT.CHECK);
			timingLimitCheck.setSelection(true);
			timingLimitCheck.addListener(SWT.Selection, checkBoxListener);
			timeLimitText = new Text(labelsComposite, textStyle);
			timeLimitLabel.setText("Set the time limit for this trace");
			timeLimitText.setText("20");
			timeLimitText.setLayoutData(newTextGridData());
			timeLimitText.addModifyListener(modifyListener);
			
			Composite fileSetterComposite = new Composite (composite, SWT.NONE);
			GridLayout filesCompLayout = new GridLayout(3, false);
			fileSetterComposite.setLayout(filesCompLayout);
			
			GridData fileSetterGridData = new GridData();
			fileSetterGridData.grabExcessHorizontalSpace = true;
			fileSetterGridData.horizontalAlignment = GridData.FILL;
			fileSetterComposite.setLayoutData(fileSetterGridData);
			fileSetterGridData.minimumWidth = 100;
			
			traviandoFileSetter = new FileSetter(fileSetterComposite, 
					"Traviando export file:", "xml");
			bioNessieFileSetter = new FileSetter(fileSetterComposite,
					"BioNessie export file:", "bn");
			sbrmlFileSetter = new FileSetter(fileSetterComposite,
					"SBRML results export file:", "sbrml");
			
			displayCommentsButton = new Button (composite, SWT.CHECK);
			displayCommentsButton.setText("Output Comments");
			displayCommentsButton.setSelection(false);
			
			Label explainComment = new Label (composite, labelStyle);
			explainComment.setText("Enter a comment for what the model" +
					" does(this isn't hugely important)");
			modelCommentText = new Text (composite, SWT.LEFT | SWT.BORDER);
			modelCommentText.setText("Trace generated from BioPEPA Eclipse Plugin");
			
			displayGraph = new Button (composite, SWT.CHECK);
			displayGraph.setText ("Show results graph");
			displayGraph.setSelection(false);
			displayGraph.addListener(SWT.Selection, checkBoxListener);
			
			
			Label explainDataPoints = new Label (composite, labelStyle);
			explainDataPoints.setText(
					"Set the increment in data point size for the graph (default 1.0)");
			// explainDataPoints.setEnabled(false);
			
			dataPointsText = new Text (composite, textStyle);
			dataPointsText.setText("");
			dataPointsText.setLayoutData(newTextGridData());
			dataPointsText.addModifyListener(modifyListener);
			
			Label explainNumberRuns = new Label (composite, labelStyle);
			explainNumberRuns.setText("The number of independent runs (default 1)");
			
			numberRunsText = new Text (composite, textStyle);
			numberRunsText.setText("");
			numberRunsText.setLayoutData(newTextGridData());
			numberRunsText.addModifyListener(modifyListener);
			
			enableWidgets();
			validate();
		}

		private class FileSetter {
			private Label cfLabel;
			private IPath cfPath;
			private Button cfButton; 
			// private String fileExtension;
			
			FileSetter (Composite parent, String exportName, String extension){
				Label exportLabel = new Label (parent, labelStyle);
				exportLabel.setText(exportName);
				cfLabel = new Label (parent, labelStyle);
				cfLabel.setText("no file");
				
				GridData labelGridData = new GridData();
				labelGridData.grabExcessHorizontalSpace = true;
				labelGridData.horizontalAlignment = GridData.FILL;
				cfLabel.setLayoutData(labelGridData);
				
				
			    GridData buttonGridData = new GridData ();
			    buttonGridData.horizontalAlignment = GridData.FILL;
			    buttonGridData.minimumWidth = 5;
			    buttonGridData.grabExcessHorizontalSpace = true;
				cfButton = new Button (parent, SWT.PUSH);
				cfButton.setText("set file");
				cfButton.setLayoutData(buttonGridData);
				
				// this.fileExtension = extension;
				
				class SetThisFile implements SelectionListener {

					private String extension;
					SetThisFile (String extension){
						super();
						this.extension = extension;
					}
					
					public void widgetDefaultSelected(SelectionEvent e) {
						return;
					}

					public void widgetSelected(SelectionEvent e) {
						SaveAsDialog saveAsDialog = new SaveAsDialog(getShell());
						IPath path = model.getUnderlyingResource().getFullPath();
						
						path = path.removeFileExtension();
						if (!extension.isEmpty()){
							path = path.addFileExtension(extension);
						}
						IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
						saveAsDialog.setOriginalFile(file);
						saveAsDialog.open();
						path = saveAsDialog.getResult();
						cfPath = path;
						cfLabel.setText(path.lastSegment());
						cfButton.setText("change");
					}
					
				}
				cfButton.addSelectionListener(new SetThisFile(extension));
			}
			
			public IPath getFilePath(){
				return cfPath;
			}
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
		
		private Listener checkBoxListener = new Listener() {
			public void handleEvent(Event event) {
				enableWidgets();
				validate();
			}
		};

		private void enableWidgets(){
			if (firingsLimitCheck.getSelection()){
				firingsLimitText.setEnabled(true);
			} else {
				firingsLimitText.setEnabled(false);
			}
			if (timingLimitCheck.getSelection()){
				timeLimitText.setEnabled(true);
			} else {
				timeLimitText.setEnabled(false);
			}
			
			if(displayGraph.getSelection()){
				dataPointsText.setEnabled(true);
			} else {
				dataPointsText.setEnabled(false);
			}
		}
		
		/*
		private Listener commonListener = new Listener() {
			public void handleEvent(Event event) {
				validate();
			}
		};
		*/

		private void validate() {
			this.setPageComplete(false);
			this.setErrorMessage(null);
			String fText = firingsLimitText.getText().trim();
			String tText = timeLimitText.getText().trim();
			
			boolean firingsChecked = firingsLimitCheck.getSelection();
			boolean timingsChecked = timingLimitCheck.getSelection();

			if (firingsChecked){
			
			try{
				Integer.parseInt(fText);
			} catch (Exception e){
				this.setPageComplete(false);
				this.setErrorMessage("Cannot parse firings limit");
				return ;
			}
			}
			if (timingsChecked){
			try{
				Double.parseDouble(tText);
			} catch (Exception e){
				this.setPageComplete(false);
				this.setErrorMessage("Cannot parse time limit");
				return ;
			}
			}
			
			if(!firingsChecked && !timingsChecked){
				this.setPageComplete(false);
				this.setErrorMessage("You must set one kind of limit");
				return ;
			}

			if (displayGraph.getSelection()) {
				String text = dataPointsText.getText().trim();
				if (!text.isEmpty()) {
					try {
						Double.parseDouble(text);
					} catch (Exception e) {
						this.setPageComplete(false);
						this.setErrorMessage("Cannot parse data points");
						return;
					}
				}
			}

			String runsText = numberRunsText.getText().trim();
			if (!runsText.isEmpty()){
				try {
					Integer.parseInt(runsText);
				} catch (Exception e) {
					this.setPageComplete(false);
					this.setErrorMessage ("Cannot parse number of runs");
					return ;
				}
			}
			
			this.setPageComplete(true);
		}

		public int getFiringsLimit() {
			if (firingsLimitCheck.getSelection()) {
				String text = firingsLimitText.getText().trim();
				try {
					return Integer.parseInt(text);
				} catch (Exception e) {
					return 0;
				}
			} else {
				return Integer.MAX_VALUE;
			}
		}
		
		public double getTimeLimit() {
			if (timingLimitCheck.getSelection()) {
				String text = timeLimitText.getText().trim();
				try {
					return Double.parseDouble(text);
				} catch (Exception e) {
					return 0;
				}
			} else {
				return Double.MAX_VALUE;
			}
		}

		public int getNumberRuns (){
			String runsText = numberRunsText.getText().trim();
			if (!runsText.isEmpty()){
				try{
					return Integer.parseInt(runsText);
				} catch (Exception e){
					return 1;
				}
			} else {
				return 1;
			}
		}
		
		public IPath getTraviandoFilePath(){
			return traviandoFileSetter.getFilePath();
		}
		public IPath getBioNessieFilePath(){
			return bioNessieFileSetter.getFilePath();
		}
		public IPath getSBRMLFilePath (){
			return sbrmlFileSetter.getFilePath();
		}
		
		
		public boolean getDisplayComments(){
			return displayCommentsButton.getSelection();
		}
		
		public String getModelComment (){
			if (modelCommentText == null){
				return "";
			}
			return modelCommentText.getText();
		}
		
		public boolean getDisplayGraph(){
			return displayGraph.getSelection();
		}
		
		public double getDataPointSize() {
			if (displayGraph.getSelection()) {
				String text = dataPointsText.getText().trim();
				if (text.isEmpty()) {
					return 1.0;
				} else {
					try {
						return Double.parseDouble(text);
					} catch (Exception e) {
						return 0;
					}
				}
			} else {
				return Double.MAX_VALUE;
			}
		}
		
		private GridData createDefaultGridData() {
			/* ...with grabbing horizontal space */
			return new GridData(SWT.FILL, SWT.CENTER, true, false);
		}
	}
	
	public void addPages (){
		exportPage = new ExportPage("Export a Traviando Trace");
		addPage (exportPage);
		phasesPage = new PhasesPage (model);
		addPage (phasesPage);
	}
	
	/*
	 * The code for creating trace loggers is a bit muddled due to
	 * the fact that we must work if are creating a trace for
	 * only traviando, only bioNessie or both or neither.
	 * The thing to watch out here is that createTraviandoTraceLogger
	 * does add the output file to the traceJob so only call this once
	 * and only if you add the traviando trace logger produced to the
	 * simulation tracer.
	 */
	public TraviandoTraceLog createTraviandoTraceLogger(int index, 
			IPath traviandoFilePath, SimulationTraceJob traceJob){
		IPath numberPath = traviandoFilePath.removeFileExtension();
		if (index > 0){
			numberPath = numberPath.addFileExtension(Integer.toString(index));
		}
		numberPath = numberPath.addFileExtension("xml");
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(numberPath);
		
		// Then we have to set up the string consumer which will
		// write out the traviando trace strings to the file in question
		String filePath = file.getLocation().toOSString();
		
		FileStringConsumer fsc = new FileStringConsumer(filePath);
		// This allows us to set up the traviando trace logger
		TraviandoTraceLog travLog = new TraviandoTraceLog(fsc, model.getSBAModel());
		// We are a complete consumer so the TraviandoTraceLog will
		// handle the opening and closing of the string consumer (file)
		travLog.setCompleteConsumer(true);
		String modelName = numberPath.removeFileExtension().lastSegment();
		travLog.setModelName(modelName);
		travLog.setDisplayComments(exportPage.getDisplayComments());
		travLog.setModelComment(exportPage.getModelComment());
		
		// Finally we set the output file of the simulation trace job
		// so that it can refresh it once we are done.
		traceJob.addOutputFile(file);
		
		return travLog;
	}
	
	/*
	 * See the above comment for createTraviandoTraceLogger but essentially
	 * this adds the file to the trace job so that it can be refreshed,
	 * hence only call this once and only if you really intend to create a
	 * bionessie trace log (ie. only if you definitely add it to the 
	 * simulation tracer).
	 */
	public BioNessieTraceLog createBioNessieTraceLogger(int index,
			IPath bioNessieFilePath, SimulationTraceJob traceJob){
		IPath numberPath = bioNessieFilePath.removeFileExtension();
		if (index > 0){
			numberPath = numberPath.addFileExtension(Integer.toString(index));
		}
		numberPath = numberPath.addFileExtension("bn");
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(numberPath);
		
		// Then we have to set up the string consumer which will
		// write out the traviando trace strings to the file in question
		String filePath = file.getLocation().toOSString();
		
		FileStringConsumer fsc = new FileStringConsumer(filePath);
		// This allows us to set up the bionessie trace logger
		BioNessieTraceLog travLog = new BioNessieTraceLog(fsc, model.getSBAModel());
		// We are a complete consumer so the BioNessieTraceLog will
		// handle the opening and closing of the string consumer (file)
		travLog.setCompleteConsumer(true);
		//String modelName = numberPath.removeFileExtension().lastSegment();
		
		// Finally we set the output file of the simulation trace job
		// so that it can refresh it once we are done.
		traceJob.addOutputFile(file);
		
		return travLog;
	}
	
	@Override
	public boolean performFinish() {
		SBAModel sbaModel = model.getSBAModel();
		IPath modelPath  = model.getUnderlyingResource().getFullPath();
		String modelName = modelPath.removeFileExtension().lastSegment();
		SimulationTracer simTrace = new SimulationTracer(sbaModel);
		SimulationTraceJob traceJob = new SimulationTraceJob (modelName);
		
		int numberRuns = exportPage.getNumberRuns();
		LinkedList<SimulationTraceLog> traceLoggers = new LinkedList<SimulationTraceLog>();
		
		IPath traviandoFilePath = exportPage.getTraviandoFilePath();
		IPath bioNessieFilePath = exportPage.getBioNessieFilePath();
		
		for (int index = 0; index < numberRuns; index++){			
			// First of all if we want BOTH traces we have to create both
			// trace loggers and then combine them together into a single
			// trace loggers which does both
			if (traviandoFilePath != null && bioNessieFilePath != null ){
				TraviandoTraceLog travLog;
				BioNessieTraceLog bioNessLog;
				
				travLog = createTraviandoTraceLogger(index, traviandoFilePath, traceJob);
				bioNessLog = createBioNessieTraceLogger(index, bioNessieFilePath, traceJob);
				ManyTraceLogger manyTraceLogger = new ManyTraceLogger();
				manyTraceLogger.addSimulationTraceLogger(travLog);
				manyTraceLogger.addSimulationTraceLogger(bioNessLog);
				// Then we add the many trace logger as a logger to the
				// simulation itself. Note that we cannot add them both
				// separately as that would result in two separate simulation
				// runs, where instead we wish to have two trace outputs of
				// the same simulation trace.
				traceLoggers.add(manyTraceLogger);
			} else if (traviandoFilePath != null){
				TraviandoTraceLog travLog;
				// So now we can assume that we ONLY want a traviando trace file
				// so simply create it and add it to the simulation tracer logs
				travLog = createTraviandoTraceLogger(index, traviandoFilePath, traceJob);
				traceLoggers.add(travLog);
			} else if (bioNessieFilePath != null){
				BioNessieTraceLog bioNessLog;
				// Similarly if we get here we can assume that we ONLY want a bioNessie
				// trace and hence we only create that and add it to the simulation
				// trace loggers
				bioNessLog = createBioNessieTraceLogger(index, bioNessieFilePath, traceJob);
				traceLoggers.add(bioNessLog);
			} else {
				// Finally in here we want neither to be logged so we
				// create a null logger and add that.
				NullTraceLog traceLogger = new NullTraceLog ();
				traceLoggers.add(traceLogger);
			}
		}
		
		simTrace.setDataPointStep(exportPage.getDataPointSize());
		simTrace.setFiringsLimit(exportPage.getFiringsLimit());
		simTrace.setTimeLimit(exportPage.getTimeLimit());

		// Now we set up any defined phases, if these aren't
		// defined then they should be null and the
		// simulation trace engine will take care of that and
		// create one large phase lasting the entire simulation.
		simTrace.setPhaseLines(phasesPage.getPhaseLines());

		traceJob.setSimulationTraceLoggers(traceLoggers);
		traceJob.setDoGraph(exportPage.getDisplayGraph());
		traceJob.setSimulationTracer(simTrace);
		traceJob.setSimulationTraceLoggers(traceLoggers);
		traceJob.setSbaModel(sbaModel);
		IPath sbrmlPath = exportPage.getSBRMLFilePath();
		if (sbrmlPath != null){
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(sbrmlPath);
			traceJob.setSbrmlFile(file);
			traceJob.setSbrmlPath(sbrmlPath);
		}
		traceJob.schedule();
		return true;
	}

	public IResource getUnderlyingResource() {
		return model.getUnderlyingResource();
	}
}
