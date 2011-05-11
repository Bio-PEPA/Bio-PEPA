package uk.ac.ed.inf.biopepa.core.sba;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ExperimentSet {

	/*
	 * An experiment set consists of an array of values where each value
	 * contains a 'line' of data which sets all the process concentrations,
	 * rates and reactions as well as providing a name for that line of the
	 * experiment. The name is used either to prefix the name of the produced
	 * single graph, or to prefix each line when the graphs are joined together.
	 */
	boolean separateGraphs;
	private LinkedList<ExperimentLine> experimentLines;

	/*
	 * To create a simple experiment which only solves the model as originally
	 * written down use: ExperimentSet experSet = new ExperimentSet ();
	 * experSet.addExperimentLine(experSet.emptyExperimentLine());
	 */
	public ExperimentSet() {
		this.experimentLines = new LinkedList<ExperimentLine>();
		this.separateGraphs = true;
	}

	public boolean getSeparateGraphs() {
		return this.separateGraphs;
	}

	public void setSeparateGraphs(boolean sepGraphs) {
		this.separateGraphs = sepGraphs;
	}

	public void addExperimentLine(ExperimentLine experLine) {
		this.experimentLines.add(experLine);
	}

	public LinkedList<ExperimentLine> getExperimentLines() {
		return experimentLines;
	}

	public String toCsvSummary (SBAModel model){
		return toCsvString(model, 3, 60);
	}
	
	public String toCsvString(SBAModel model){
		return toCsvString(model, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	public LinkedList<LinkedList<String>> makeListTable(SBAModel sbaModel) {
		HashSet<String> componentNames = new HashSet<String>();
		HashSet<String> rateNames = new HashSet<String>();
		HashSet<String> reactionNames = new HashSet<String>();
		
		// First work out all the names that we will be
		// adding to each line of the table.
		for (ExperimentLine experLine : this.experimentLines) {
			componentNames.addAll(experLine.getInitialPopulations().keySet());
			rateNames.addAll(experLine.getRateNames());
			reactionNames.addAll(experLine.getReactionNames());
		}
		
		// Now add them all in the correct order for the header line
		LinkedList<String> headerNames = new LinkedList<String>();
		
		headerNames.addLast("line-name");
		
		for (String cName : componentNames) {
			headerNames.addLast(cName);
		}
		for (String rName : rateNames) {
			headerNames.addLast(rName);
		}
		for (String rName : reactionNames) {
			headerNames.addLast(rName);
		}
		
		// The result list of lists to which we first add the headers
		LinkedList<LinkedList<String>> resultTable = new LinkedList<LinkedList<String>>();
		
		// Add the headers to this list.
		resultTable.addLast(headerNames);
		
		// The for each experiment line add the corresponding row.
		// For each line output the names it defines and for
		// those it does not the default
		for (ExperimentLine experLine : this.experimentLines) {
			LinkedList<String> values = new LinkedList<String>();
			// First add the experiment line name
			values.addLast(experLine.getName());
			for (String compName : componentNames) {
				Number pop = experLine.getInitialPopulation(compName);
				Number defaultPop = sbaModel.getNamedComponentCount(compName);
				String popString = (pop != null) ? pop.toString() : defaultPop.toString();
				values.addLast(popString);
			}

			for (String rateName : rateNames) {
				Number rate = experLine.getRateValue(rateName);
				Number defaultRate = 0; // But should obviously get from model
				String rateString = (rate != null) ? rate.toString() : 
					(defaultRate != null) ? defaultRate.toString() : "0";
				values.addLast(rateString);
			}

			for (String rName : reactionNames) {
				if (experLine.isReactionActiviated(rName)) {
					values.addLast("on");
				} else {
					values.addLast("off");
				}
			}
			// Finally add this line to the result table
			resultTable.addLast(values);
		}
		return resultTable;
	}
	
	
	
	public String toCsvString(SBAModel model, int maxLines, int maxWidth) {
		// if (model != null) return "hey hey hey";
		LineStringBuilder sb = new LineStringBuilder();
		HashSet<String> componentNames = new HashSet<String>();
		HashSet<String> rateNames = new HashSet<String>();
		HashSet<String> reactionNames = new HashSet<String>();

		// Make sure we only convert as many lines as we wish
		// and no more.
		List <ExperimentLine> theseLines = experimentLines;
		if (experimentLines.size() > maxLines){
			theseLines = experimentLines.subList(0, maxLines - 1);
		}
		
		
		// First work out all of the names,
		// this needn't be all the names in the model
		// only those mentioned in the experiments
		for (ExperimentLine experLine : theseLines) {
			componentNames.addAll(experLine.getInitialPopulations().keySet());
			rateNames.addAll(experLine.getRateNames());
			reactionNames.addAll(experLine.getReactionNames());
		}

		StringBuilder header = new StringBuilder ();
		// Print out the header line
		header.append("line-name");
		// Using the for syntax here and when we print out each
		// line assumes that this code iterates through the names
		// in the same order.
		for (String cName : componentNames) {
			header.append(", " + cName);
		}
		for (String rName : rateNames) {
			header.append(", " + rName);
		}
		for (String rName : reactionNames) {
			header.append(", " + rName);
		}
		// Now if the line is too long truncate it
		String headerString = header.toString();
		if(header.length() > maxWidth){
			headerString = headerString.substring(0, maxWidth - 5);
			headerString = headerString.concat(" ...");
		}
		// add the header line to the cumulative string builder
		sb.appendLine(headerString);

		// For each line output the names it defines and for
		// those it does not the default
		for (ExperimentLine experLine : theseLines) {
			LineStringBuilder thisLine = new LineStringBuilder();
			thisLine.append(experLine.getName());
			for (String compName : componentNames) {
				Number pop = experLine.getInitialPopulation(compName);
				Number defaultPop = model.getNamedComponentCount(compName);
				String popString = (pop != null) ? pop.toString() : defaultPop.toString();
				thisLine.append(", " + popString);
			}

			for (String rateName : rateNames) {
				Number rate = experLine.getRateValue(rateName);
				Number defaultRate = 0; // But should obviously get from model
				String rateString = (rate != null) ? rate.toString() : (defaultRate != null) ? defaultRate.toString()
						: "0";
				thisLine.append(", " + rateString);
			}

			for (String rName : reactionNames) {
				if (experLine.isReactionActiviated(rName)) {
					thisLine.append(", on");
				} else {
					thisLine.append(", off");
				}
			}

			// Make sure we truncate the line if we are past the max length
			String thisLineString = thisLine.toString();
			if(thisLineString.length() > maxWidth){
				thisLineString = thisLineString.substring(0, maxWidth - 5);
				thisLineString = thisLineString.concat(" ...");
			}
			
			// Finally remember to add this line to the cumulative
			// stringbuilder and end the line
			sb.appendLine(thisLine.toString());
		}

		if (experimentLines.size() > theseLines.size()){
			sb.appendLine(" ... ");
		}
		return sb.toString();
	}

}

