package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import uk.ac.ed.csbe.sbsivisual.sbsiDataFormat.*;
import uk.ac.ed.inf.biopepa.core.BasicResult;
import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.sba.Parameters;

import au.com.bytecode.opencsv.CSVReader;

public class ImportDataPage extends WizardPage {

	private int labelStyle = SWT.SINGLE | SWT.LEFT;
	
	protected ImportDataPage(String pageName) {
		super(pageName);
		this.setTitle("Import external data for comparison");
		this.setDescription("Import data from a csv file or SBSI format" +
				" to plot alongside data from BioPEPA analysis");
	}

	private FileOpener csvFileOpener;
	private FileOpener sbsiResultParser;
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		setControl(composite);
		
		Composite fileSetterComposite = new Composite (composite, SWT.NONE);
		GridLayout filesCompLayout = new GridLayout(3, false);
		fileSetterComposite.setLayout(filesCompLayout);
		
		GridData fileSetterGridData = new GridData();
		fileSetterGridData.grabExcessHorizontalSpace = true;
		fileSetterGridData.horizontalAlignment = GridData.FILL;
		fileSetterComposite.setLayoutData(fileSetterGridData);
		fileSetterGridData.minimumWidth = 100;
		
		csvFileOpener = new FileOpener (fileSetterComposite, 
				"Open csv data", new CsvResultParser ());
		
		sbsiResultParser = new FileOpener (fileSetterComposite,
				"Open sbsi data", new SBSIResultParser());
		
	}
	
	private interface ResultFileParser {
		public void parseFile (String filename);
		public String[] getFileExtensions();
	}
	
	private class CsvResultParser implements ResultFileParser {
		public void parseFile (String filename){
			readCsvFile(filename);
		}
		public String[] getFileExtensions (){
			String [] result = { "*.csv", "*" };
			return result;
		}
	}
	
	private class SBSIResultParser implements ResultFileParser {
		public void parseFile (String filename){
			readSBSIFile(filename);
		}
		public String[] getFileExtensions (){
			String [] result = { "*.dat", "*" };
			return result;
		}
	}
	
	private class FileOpener {
		private Label cfLabel;
		private Button cfButton;
		private ResultFileParser resultFileParser;
		
		String selectedFile;
		
		public void unSetSelectedFile(){
			cfLabel.setText("no file");
			cfButton.setText("set file");
		}
		
		FileOpener (Composite parent, String exportName, 
				ResultFileParser resultFileParser){
			Label exportLabel = new Label (parent, labelStyle);
			exportLabel.setText(exportName);
			
			GridData buttonGridData = new GridData ();
		    buttonGridData.horizontalAlignment = GridData.FILL;
		    buttonGridData.minimumWidth = 5;
		    buttonGridData.grabExcessHorizontalSpace = true;
			cfButton = new Button (parent, SWT.PUSH);
			cfButton.setText("set file");
			cfButton.setLayoutData(buttonGridData);
			String[] extensions = resultFileParser.getFileExtensions();
			cfButton.addSelectionListener(new Open(extensions));
			
			cfLabel = new Label (parent, labelStyle);
			cfLabel.setText("no file");
			
			GridData labelGridData = new GridData();
			labelGridData.grabExcessHorizontalSpace = true;
			labelGridData.horizontalAlignment = GridData.FILL;
			cfLabel.setLayoutData(labelGridData);
			
	        this.resultFileParser = resultFileParser;
		}
		
		class Open implements SelectionListener {
			private String[] extensions;
			
			Open(String[] extensions){
				this.extensions = extensions;
			}
			
			public void widgetSelected(SelectionEvent event) {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				fd.setText("Open");
				fd.setFilterPath("C:/");
				fd.setFilterExtensions(this.extensions);
				String selected = fd.open();
				IPath selectedPath = Path.fromOSString(selected);
				String labelString = selectedPath.lastSegment();
				if (labelString.length() > 30){
					labelString = labelString.substring(0, 30) + "...";
				}
				selectedFile = selected;
				// cfLabel.setText(selectedFile);
				cfLabel.setText(labelString);
				cfButton.setText("Change");
				resultFileParser.parseFile(selectedFile);
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}
	}
	
	public Result getPlottableResult(){
		return this.plottableResult;
	}
	private Result plottableResult;
	
	
	private void csvParseError(String errMsg){
		this.csvFileOpener.unSetSelectedFile();
		this.setErrorMessage(errMsg);
	}
	
	private void readCsvFile (String filename){
		if (filename == null) {
			// csvReadError = "Filename null";
			return ;
		}
		try {
			FileReader freader = new FileReader(filename);
			CSVReader reader = new CSVReader(freader);

			// Read the top line which should give us the
			// component and rate names
			String[] nextLine;
			nextLine = reader.readNext();
			if (nextLine == null) {
				return;
			}
			// The first line specifies the names, we assume that
			// the first name is time.
			String[] names = new String[nextLine.length - 1];
			for (int i = 1; i < nextLine.length; i++) {
				names[i - 1] = nextLine[i].trim();
			}
			
			LinkedList<Number>timesList = new LinkedList<Number>();
			// For each time point this will hold the value of each
			// component's name. Note that this is kind of the other
			// way around from how a 'Result' is held.
			LinkedList<double[]>resultsList = new LinkedList<double[]>();
			
			while ((nextLine = reader.readNext()) != null) {
				timesList.add(Double.parseDouble(nextLine[0].trim()));
				double [] resultLine = new double [names.length];
				for (int column = 0; column < names.length; column++){
					if (column > nextLine.length){
						resultLine[column] = 0;
					} else {
						double value = Double.parseDouble(nextLine[column + 1].trim());
						resultLine[column] = value;
					}
				}
				resultsList.addLast(resultLine);
			}
			double [] times = new double[timesList.size()];
			for (int index = 0; index < times.length; index++){
				times[index] = timesList.get(index).doubleValue();
			}
			// For each component name create a results array the same
			// length as the time points array
			double [][] results = new double[names.length][];
			for (int i = 0; i < names.length; i++){
				results[i] = new double[times.length];
			}
			for (int timeIndex = 0; timeIndex < times.length; timeIndex++){
				double[] resultLine = resultsList.get(timeIndex);
				for (int index = 0; index < results.length; index++){
					results[index][timeIndex] = resultLine[index];
				}
			}
			
			Parameters params = new Parameters();
			HashMap<String, Number> modelParams = new HashMap<String, Number>();
			BasicResult thisResult = new BasicResult(params, modelParams);
			thisResult.setComponentNames(names);
			thisResult.setResults(results);
			thisResult.setTimePoints(times);
			
			this.setErrorMessage(null);
			plottableResult = thisResult;
		} catch (FileNotFoundException e) {
			this.csvParseError("csv file not found");
			e.printStackTrace();
		} catch (IOException e) {
			this.csvParseError("csv file / io error " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e){
			this.csvParseError("csv file error " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	private void sbsiParseError(String errMsg){
		this.sbsiResultParser.unSetSelectedFile();
		this.setErrorMessage(errMsg);
	}
	private void readSBSIFile (String filename){
		if (filename == null) {
			// System.out.println("Filename is null");
			return ;
		}
		try {			
			SBSIResourceFactory sbsiResFact = SBSIResourceFactory.getInstance();
			
			
			
			SBSIDataDocument sbsiDoc = sbsiResFact.readSBSIDataFile(filename);
			ISBSIData sbsiData = sbsiDoc.getSBSIData();
						
			String [] names = sbsiData.getHeaders();
			double [][] results = new double[names.length][];
			for (int index = 0; index < names.length; index++){
				results[index] = sbsiData.getColumnData(index);
			}
			double [] times = results[0];
			
			Parameters params = new Parameters();
			HashMap<String, Number> modelParams = new HashMap<String, Number>();
			BasicResult thisResult = new BasicResult(params, modelParams);
			thisResult.setComponentNames(names);
			thisResult.setResults(results);
			thisResult.setTimePoints(times);
			
			this.setErrorMessage(null);
			
			plottableResult = thisResult;
		} catch (IllegalStateException e){
			// We assume this means it can't parse the file.
			this.sbsiParseError("Cannot parse sbsi data file " +
					e.getMessage());
		} catch (Exception e){
			this.sbsiParseError("Error opening sbsi data file " +
					e.getMessage());
			e.printStackTrace();
		}
	}
}
