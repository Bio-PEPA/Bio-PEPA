package uk.ac.ed.inf.biopepa.core.sba;

import java.io.*;

import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import au.com.bytecode.opencsv.CSVReader;

public class ExperimentSetReader {

	private String csvReadError = "";
	private ExperimentSet resultSet = null;
	private SBAModel sbaModel;
	private ModelCompiler modelCompiler;
	
	public ExperimentSetReader (SBAModel sbam, ModelCompiler mc){
		this.sbaModel = sbam;
		this.modelCompiler = mc;
	}
	
	public String getReadError(){
		return csvReadError;
	}
	public ExperimentSet getExperimentSet(){
		return resultSet;
	}
	
	public boolean arrayContains(Object[] array, Object object) {
		for (Object arrayObj : array) {
			if (arrayObj.equals(object)) {
				return true;
			}
		}
		return false;
	}

	
	public void readCsvFile (String filename, String[] specialNames){
		csvReadError = "";
		resultSet = null;
		if (filename == null) {
			csvReadError = "Filename null";
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
			String[] names = new String[nextLine.length];
			for (int i = 0; i < nextLine.length; i++) {
				names[i] = nextLine[i].trim();
			}

			
			// We need the names of the reactions, it's possible
			// that we could also do this will 
			// modelCompiler.containsFunctionalRate
			// so I should try that.
			SBAReaction[] modelReactions = sbaModel.getReactions();
			String[] modelReactionNames = new String[modelReactions.length];
			for (int i = 0; i < modelReactions.length; i++) {
				modelReactionNames[i] = modelReactions[i].getName();
				// System.out.println(modelReactionNames[i]);
			}

			ExperimentSet experSet = new ExperimentSet();
			// Now for each remaining line enter it in the experiment set
			int i = 0;

			while ((nextLine = reader.readNext()) != null) {
				if (nextLine.length != names.length) {
					csvReadError = ("Parse Error; could not parse the csv file");
					return;
				}
				// This is the default line name
				String defaultLineName = "Ex-" + i;
				ExperimentLine experimentLine = new ExperimentLine(defaultLineName);

				for (int j = 0; j < names.length; j++) {
					if (names[j].trim().equals("line-name")) {
						experimentLine.setName(nextLine[j].trim());
					} else if (arrayContains(specialNames, names[j])) {
						double value = Double.parseDouble(nextLine[j]);
						experimentLine.addSpecialDefine(names[j], value);
					} else if (modelCompiler.containsComponent(names[j])) {
						double value = Double.parseDouble(nextLine[j]);
						experimentLine.addInitialConcentration(names[j], value);
					} else if (arrayContains(modelReactionNames, names[j])) {
						if (nextLine[j].trim().equals("off")) {
							experimentLine.addReactionActivation(names[j],
									false);
						} else if (nextLine[j].trim().equals("on")) {
							experimentLine
									.addReactionActivation(names[j], true);
						} else {
							csvReadError = "Reaction activation unrecognised" +
											" Your line is:"
										+ nextLine[j]
										+ "it must be either \"on\" or \"off\"";
							return;
						}
					} else if (modelCompiler.containsVariable(names[j])) {
						double value = Double.parseDouble(nextLine[j]);
						experimentLine.addRateValue(names[j], value);
					} else {
						csvReadError = "Not-found; " 
							+ "The name: " + names[j]
							+ " mentioned in the csv file is"
							+ " not found in the model";
						return;
					}
				}
				experSet.addExperimentLine(experimentLine);
				// nextLine[] is an array of values from the line
				// System.out.println(nextLine[0] + nextLine[1] + "etc...");
				i++;
			}
			this.resultSet = experSet;
			return;
		} catch (FileNotFoundException e) {
			this.resultSet = null;
			this.csvReadError = "File not found";
			e.printStackTrace();
		} catch (IOException e) {
			this.resultSet = null;
			e.printStackTrace();
		}
	}
}