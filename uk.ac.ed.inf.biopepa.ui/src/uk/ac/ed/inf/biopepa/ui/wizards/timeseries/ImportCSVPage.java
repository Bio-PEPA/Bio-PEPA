package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.sedml.Model;
import org.sedml.Output;
import org.sedml.Plot2D;
import org.sedml.SEDMLDocument;
import org.sedml.SedML;
import org.sedml.modelsupport.SUPPORTED_LANGUAGE;

import uk.ac.ed.inf.biopepa.core.sba.ExperimentSet;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentSetReader;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;

public class ImportCSVPage extends WizardPage {

	public final static String wizardPageName = "Import CSV Page";

	private BioPEPAModel model;

	ExperimentSet experimentSet;
	String loadedFileName;

	Shell shell;

	public ImportCSVPage(BioPEPAModel model) {
		super(wizardPageName);
		this.model = model;
		this.shell = new Shell();
		// this.experimentSet = null ;
		setTitle("Import experimentation setup from cvs");
		setDescription("Set up experimentation from csv");
	}

	private Button separateGraphsButton;
	private Button justBuildCsvButton;
	private Button saveExperButton;
	private Combo saveExperCombo; 
	private Button runRemoteExperButton;

	public boolean getSeparateGraphs() {
		return separateGraphsButton.getSelection();
	}
	
	public boolean getJustBuildCsv(){
		return justBuildCsvButton.getSelection();
	}

	// private Label showLoadedLabel;

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

		separateGraphsButton = new Button(composite, SWT.CHECK);
		separateGraphsButton.setText("separate graphs");
		separateGraphsButton.setSelection(true);

		justBuildCsvButton = new Button(composite, SWT.CHECK);
		justBuildCsvButton.setText("Straight to csv (no graph)");
		justBuildCsvButton.setSelection(false);

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

		Button createCsvButton = new Button(composite, SWT.PUSH);
		createCsvButton.setText("Create Experiment");

		class Create implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				WizardDialog dialog = null;
				try {
					CreateExperimentationWizard wizard = new CreateExperimentationWizard(model);
					Shell shell = Display.getDefault().getActiveShell();
					dialog = new WizardDialog(shell, wizard);
					dialog.open();
					ExperimentSet experSet = wizard.getExperimentSet();
					clearLoaded();
					loadExperimentSet(experSet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}
		createCsvButton.addSelectionListener(new Create());

		
		Composite saveExperComposite = new Composite (composite, SWT.NONE);
		int numColumns = 2;
		Layout compositeLayout= new GridLayout(numColumns, false);
		saveExperComposite.setLayout(compositeLayout);
		
		// Save the loaded experiment as a csv file
		// We must create this before we set the createCSVbutton's
		// selection listener such that it can enable this button.
		saveExperButton = new Button(saveExperComposite, SWT.PUSH);
		saveExperButton.setText("Save loaded experiment");
		saveExperButton.setEnabled(false);

		class SaveCSV implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				if (saveExperCombo.getSelectionIndex() == 0){
					saveLoadedExperiment();
				} else {
					saveLoadedExperimentSedML();
				}
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}
		saveExperButton.addSelectionListener(new SaveCSV());

		saveExperCombo = new Combo(saveExperComposite, SWT.READ_ONLY);
		String[] choices = { "csv", "SedML" };
		saveExperCombo.setItems(choices);
		saveExperCombo.select(0);
		
		runRemoteExperButton = new Button (composite, SWT.PUSH);
		runRemoteExperButton.setText("Run loaded experiment remotely");
		runRemoteExperButton.setEnabled(false);
		class RunRemoteExperiment implements SelectionListener {
			/*
			 * TODO: Currently this just does the same as the save
			 * loaded experiment above, but should translate the
			 * ExperimentSet into a JamesII - BaseExperiment and then
			 * run it remotely.
			 */
			public void widgetSelected(SelectionEvent event) {
				if (saveExperCombo.getSelectionIndex() == 0){
					saveLoadedExperiment();
				} else {
					saveLoadedExperimentSedML();
				}
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}
		runRemoteExperButton.addSelectionListener(new RunRemoteExperiment());
		/*
		showLoadedLabel = new Label(composite, SWT.BORDER);
		showLoadedLabel.setText("No experiment loaded");
		showLoadedLabel.setSize(100, 100);
		// GridData loadedLabelGrid = new GridData (1, false);
		*/

		/*
		Table table = new Table(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText("");
		column.setWidth(80);

		column = new TableColumn(table, SWT.LEFT);
		column.setText("");
		column.setWidth(180);
        */

		// Create the viewer and connect it to the view
		// csvTableViewer = new TableViewer(table);

		// To get anything displayed in the table you must provide two things
		// ~ the content
		// This is done by an
		// org.eclipse.jface.viewers.IStructuredContentProvider
		// ~ the labels to be displayed in the table cells
		// This is delegated to an ITableLabelProvider

		// csvTableViewer.setContentProvider(new ArrayContentProvider());
		// csvTableViewer.setLabelProvider(new MyLabelProvider());
	}

	// private TableViewer csvTableViewer;

	/**
	 * A very simple private LabelProvider. Does not support images.
	 */
	private class MyLabelProvider extends LabelProvider implements ITableLabelProvider {
		/**
		 * We return null, because we don't support images yet.
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			LinkedList<String>row = (LinkedList<String>) element;
			return row.get(columnIndex);
		}
	}
    
	public static void renewTable(Table table) {
		for (TableColumn tblCol : table.getColumns()){
			tblCol.dispose();
		}
		// table.clearAll(); not required
		table.removeAll();
	}
	
	/*
	void updateLoadedTable (){
		Table table = csvTableViewer.getTable();
		renewTable (table);
		table.setHeaderVisible(true);
		if (this.experimentSet == null){
			TableColumn column = new TableColumn(table, SWT.LEFT);
			column.setText("No experiment loaded");
			column.setWidth(100);
		} else {
			LinkedList<LinkedList<String>> stringTable = 
				experimentSet.makeListTable(model.getSBAModel());
			for (String columnName : stringTable.getFirst()){
				TableColumn column = new TableColumn (table, SWT.LEFT);
				column.setText(columnName);
				column.setWidth(50);
			}
			LinkedList<String>[] tableArray = new LinkedList[stringTable.size() - 1];
			for (int index = 0; index < tableArray.length; index++){
				tableArray[index] = stringTable.get(index + 1);
			}
			
			csvTableViewer.setInput(tableArray);
			csvTableViewer.refresh(true, true);
			table.setSize(500, 500);
			table.update();
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
		}
	}*/
   
	/*
	private void updateLoadedLabel (){
		LineStringBuilder sb = new LineStringBuilder();
		if (this.experimentSet == null){
			showLoadedLabel.setText("No experiment loaded");
		} else {
			if (this.loadedFileName == null){
				sb.appendLine("no associated file blarkk");
				sb.appendLine(experimentSet.toCsvSummary(model.getSBAModel()));
			} else {
				Path filePath = new Path (this.loadedFileName);
				String filename = filePath.lastSegment();
				sb.appendLine("file: " + filename);
			}
			sb.appendLine(experimentSet.toCsvSummary(model.getSBAModel()));
		}
		showLoadedLabel.setText(sb.toString());
		showLoadedLabel.redraw();
	}
	*/
	
	private boolean saveLoadedExperiment() {
		SaveAsDialog saveAsDialog = new SaveAsDialog(getShell());
		IPath path = model.getUnderlyingResource().getFullPath();
		path = path.removeFileExtension().addFileExtension("csv");
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		saveAsDialog.setOriginalFile(file);
		saveAsDialog.open();
		path = saveAsDialog.getResult();
		if (path == null)
			return false;
		path = path.removeFileExtension().addFileExtension("csv");
		file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		String contents = null;
		try {
			contents = experimentSet.toCsvString(model.getSBAModel());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		InputStream source = new ByteArrayInputStream(contents.getBytes());
		try {
			if (file.exists()) {
				file.setContents(source, IResource.NONE, null);
				this.loadedFileName = file.getName();
			} else {
				file.create(source, IResource.NONE, null);
			}
			file.refreshLocal(0, null);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		// updateLoadedTable();
		return true;
	}
	
	private boolean saveLoadedExperimentSedML (){
		SaveAsDialog saveAsDialog = new SaveAsDialog(getShell());
		IPath path = model.getUnderlyingResource().getFullPath();
		path = path.removeFileExtension().addFileExtension("xml");
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		saveAsDialog.setOriginalFile(file);
		saveAsDialog.open();
		path = saveAsDialog.getResult();
		if (path == null)
			return false;
		path = path.removeFileExtension().addFileExtension("xml");
		file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		String contents = null;
		
		ExperimentSedMLExport sedmlExport = 
			new ExperimentSedMLExport (this.experimentSet, 
					                   model.getSBAModel());
		SEDMLDocument sedMLDoc = sedmlExport.exportToSedML();
		
		/*
		 * Should we validate the returned document?
		 * Or should the exporter to that itself?
		try {
			for (SedMLError err : sedMLDoc.validate()){
				System.out.println(err.getMessage());
			}
		} catch (XMLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		try {
			contents = sedMLDoc.writeDocumentToString();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		InputStream source = new ByteArrayInputStream(contents.getBytes());
		try {
			if (file.exists()) {
				file.setContents(source, IResource.NONE, null);
			} else {
				file.create(source, IResource.NONE, null);
			}
			file.refreshLocal(0, null);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		// Finally here, we must have enjoyed success
		return true;
	}	
	

	/*
	 * Load an experiment set, generally one generated from the create
	 * experiment wizard rather than one read in from an existing csv file,
	 * because we are going to enable the button for saving as a csv file.
	 */
	private void loadExperimentSet(ExperimentSet experSet) {
		if (experSet != null) {
			this.experimentSet = experSet;
			this.setMessage("Experiment loaded");
			if (saveExperButton != null) {
				saveExperButton.setEnabled(true);
			}
			if (runRemoteExperButton !=null) {
				runRemoteExperButton.setEnabled(true);
			}
		} else {
			if (saveExperButton != null) {
				saveExperButton.setEnabled(false);
			}
			if (runRemoteExperButton != null){
				runRemoteExperButton.setEnabled(false);
			}
		}
		// updateLoadedTable();
	}

	public ExperimentSet getExperimentSet() {
		return this.experimentSet;
	}

	public boolean arrayContains(Object[] array, Object object) {
		for (Object arrayObj : array) {
			if (arrayObj.equals(object)) {
				return true;
			}
		}
		return false;
	}

	public void clearLoaded() {
		this.experimentSet = null;
		this.loadedFileName = null;
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
			return;
		}
		ExperimentSetReader esetReader = 
			new ExperimentSetReader (model.getSBAModel(), 
				model.getCompiledModel());
		esetReader.readCsvFile(filename, new String[0]);
		String readingError = esetReader.getReadError();
		ExperimentSet experSet = esetReader.getExperimentSet();
		if (experSet != null){
			experSet.setSeparateGraphs(separateGraphsButton.getSelection());
			this.experimentSet = experSet;
			this.loadedFileName = filename;
			this.setMessage("The csv file: " + filename + " has been loaded");
			// this.updateLoadedTable();
			return;
		} else {
			MessageDialog.openError(shell, "Error importing csv", readingError);
			clearLoaded();
			return;
		}
	}
}
