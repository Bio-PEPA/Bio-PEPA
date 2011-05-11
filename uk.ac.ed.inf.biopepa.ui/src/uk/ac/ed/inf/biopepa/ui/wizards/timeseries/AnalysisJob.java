/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.interfaces.Solver;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentSet;
import uk.ac.ed.inf.biopepa.core.sba.Parameters;
import uk.ac.ed.inf.biopepa.core.sba.PhaseLine;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentLine;
import uk.ac.ed.inf.biopepa.ui.BioPEPAPlugin;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;
import uk.ac.ed.inf.common.ui.plotting.*;
import uk.ac.ed.inf.common.ui.plotting.data.InfoWithAxes;
import uk.ac.ed.inf.common.ui.plotting.data.Series;
import uk.ac.ed.inf.common.ui.plotview.PlotViewPlugin;

/**
 * 
 * @author ajduguid
 * 
 */
public class AnalysisJob extends Job {

	private static String term = System.getProperty("line.separator");

	BioPEPAModel model;

	Solver solver;

	Parameters parameters;

	ExperimentSet experimentation;
	private boolean justBuildCsv;
	private IPath csvPath;
	private PhaseLine[] phaseLines;

	AnalysisJob(BioPEPAModel model, Parameters parameters, 
			ExperimentSet experiments, Solver solver) {
		super("Time-series Analysis (" + solver.getShortName() + ")");
		this.model = model;
		this.parameters = parameters;
		this.solver = solver;
		this.experimentation = experiments;

		// default setting is to draw the graph.
		this.justBuildCsv = false;
		this.csvPath = null;
	}

	public void setPhaseLines(PhaseLine[] lines){
		this.phaseLines = lines;
	}
	
	public void setJustBuildCsv(boolean b) {
		this.justBuildCsv = b;
	}
	
	public void setCsvPath(IPath p) {
		this.csvPath = p;
	}
	

	
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		LinkedList<ExperimentLine> experimentLines = experimentation.getExperimentLines();
		try {
			for (ExperimentLine experLine : experimentLines) {
				if (experLine.getResult() == null){
					model.overrideAndRecompile(experLine);
					Result result;
					if (phaseLines == null){
						result = model.timeSeriesAnalysis(solver, 
														parameters, 
														monitor);
					} else {
						result = model.runPhasesTimeSeries(solver, 
															parameters, 
															monitor, 
															phaseLines);
					}
					experLine.setResult(result);
				}
			}
		} catch (final CoreException e) {
			return e.getStatus();
		} catch (BioPEPAException e){
			return new Status(IStatus.ERROR, BioPEPAPlugin.PLUGIN_ID, e.getMessage());
		}
		try {
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;

			// Now we draw the graphs, either separately or
			// as a single graph.
			if (experimentation.getSeparateGraphs()) {
				for (ExperimentLine experLine : experimentLines) {
					LinkedList<ExperimentLine> singleList = new LinkedList<ExperimentLine>();
					singleList.add(experLine);
					drawGraph(false, experLine.getName(), singleList);
				}
			} else {
				drawGraph(true, "results", experimentLines);
			}

		} catch (Exception e) {
			return new Status(IStatus.ERROR, BioPEPAPlugin.PLUGIN_ID, e.getMessage());
		}
		return Status.OK_STATUS;
	}

	
	
	// Draws a group of results to the same graph
	// If you are drawing a single result, then you probably want both
	// the variable string and the single entry string to be ""
	private void drawGraph(boolean isSingleGraph, String graphTitle, 
			LinkedList<ExperimentLine> experimentLines) {

		// Maybe I could think about throwing an exception or something.
		if (experimentLines.isEmpty()) {
			return;
		}

		/*
		 * It would be nice to avoid doing this in the common case that all
		 * have been created with the same algorithm and hence all the results
		 * are normalised anyway.
		 */
		double [] normalisedTimePoints = experimentLines.get(0).getResult().getTimePoints(); 
		for (int resultIndex = 1; resultIndex < experimentLines.size(); resultIndex++){
			Result result = experimentLines.get(resultIndex).getResult();
			result.normaliseResult(normalisedTimePoints);
		}
		
		// Collect the y information for the graph.
		InfoWithAxes info = new InfoWithAxes();
		// info.setXSeries(Series.create(results[0].getTimePoints(), "Time"));
		List<Series> list = info.getYSeries();

		for (ExperimentLine experLine : experimentLines) {
			Result result = experLine.getResult();
			info.setXSeries(Series.create(result.getTimePoints(), "Time"));
			String[] names = result.getComponentNames();
			String name = experLine.getName();
			String prefix = (isSingleGraph && name != null) ? (name + " - ") : "";
			for (int i = 0; i < names.length; i++) {
				list.add(Series.create(result.getTimeSeries(i), prefix + names[i]));
			}
		}

		info.setShowLegend(true);
		info.setYLabel("Process Count / Variable Value");
		IPath path = model.getUnderlyingResource().getFullPath();
		path.removeFileExtension();

		/*
		 * Set the graph title to essentially the name of the biopepa file, if
		 * the graph title given is not empty then we appent that onto the end
		 * of the biopepa file name.
		 */
		if (graphTitle.equals("")) {
			info.setGraphTitle(path.lastSegment());
		} else {
			info.setGraphTitle(path.lastSegment() + " - " + graphTitle);
		}
		final IChart chart = Plotting.getPlottingTools().createTimeSeriesChart(info);

		Result[] resultsArray = new Result[experimentLines.size()];
		for (int i = 0; i < experimentLines.size(); i++) {
			resultsArray[i] = experimentLines.get(i).getResult();
		}
		ResultsAdapter ra = new ResultsAdapter(resultsArray);
		chart.setSemanticElement(ra);

		// Now if we are writing out to a csv file
		// then we do so now, the path will depend on whether
		// we are to attach the prefix or not.
		// System.out.println ("Graph title is: " + graphTitle);
		if (csvPath != null) {
			IPath thisCsvPath = (IPath) csvPath.clone();
			if (!isSingleGraph) {
				thisCsvPath = thisCsvPath.removeFileExtension();
				String lastBit = thisCsvPath.lastSegment();
				thisCsvPath    = thisCsvPath.removeLastSegments(1);
				String gTitle = graphTitle.replace('.', '_');
				thisCsvPath    = thisCsvPath.append(lastBit + "_" + gTitle);
				// thisCsvPath = thisCsvPath.append("_" + graphTitle);
				// thisCsvPath = thisCsvPath.addFileExtension(graphTitle);
				thisCsvPath = thisCsvPath.addFileExtension("csv");
			}
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(thisCsvPath);
			try {
				ByteArrayInputStream is = new ByteArrayInputStream(Plotting.getPlottingTools().convertToCSV(chart));
				if (file.exists()) {
					file.setContents(is, true, false, new NullProgressMonitor());
				} else {
					file.create(is, true, new NullProgressMonitor());
				}
				// should open the file
				// org.eclipse.ui.ide.IDE.openEditor(this.view.getPage(), file);

			} catch (Exception e) {
				System.out.println(e.getMessage());
				/*
				 * ErrorDialog.openError(shell, "Error converting",
				 * "An error has occurred while converting the resource",
				 * PlotViewPlugin.wrapException("Error converting chart", e));
				 */

			}
		}		

		if (!justBuildCsv) {
			Runnable runnable = new Runnable() {
				public void run() {
					PlotViewPlugin.getDefault().reveal(chart);
				}
			};
			Display.getDefault().syncExec(runnable);
		}
	}

	private class ResultsAdapter implements ISemanticElement {

		private Result[] results;

		ResultsAdapter(Result[] results) {
			this.results = results;
		}

		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			if (adapter.equals(Result.class))
				return results;
			return null;
		}

		public String getDescription(String format) {
			StringBuilder sb = new StringBuilder();
			if (ISemanticElement.CSV_FORMAT.equals(format)) {
				for (Result result : results) {
					sb.append("# Simulator: ").append(result.getSimulatorName()).append(term);
					for (Entry<String, Number> me : result.getSimulatorParameters().entrySet())
						sb.append("# ").append(me.getKey()).append(": ").append(me.getValue().toString()).append(term);
					sb.append("# Model Parameters").append(term);
					for (Entry<String, Number> me : result.getModelParameters().entrySet())
						sb.append("# ").append(me.getKey()).append(" = ").append(me.getValue().toString()).append(term);
					// sb.delete((sb.length() - term.length()), sb.length());
					sb.append("# Run time = " + result.getSimulationRunTime()).append(term);
				}
			}
			return sb.toString();
		}

	}
}
