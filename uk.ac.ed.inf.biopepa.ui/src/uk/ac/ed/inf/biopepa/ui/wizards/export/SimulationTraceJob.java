package uk.ac.ed.inf.biopepa.ui.wizards.export;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.export.SBRMLResultExport;
import uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer;
import uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer.SimulationTraceLog;
import uk.ac.ed.inf.biopepa.ui.BioPEPAPlugin;
import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotting.ISemanticElement;
import uk.ac.ed.inf.common.ui.plotting.Plotting;
import uk.ac.ed.inf.common.ui.plotting.data.InfoWithAxes;
import uk.ac.ed.inf.common.ui.plotting.data.Series;
import uk.ac.ed.inf.common.ui.plotview.PlotViewPlugin;

public class SimulationTraceJob extends Job {
	private String jobName;
	public SimulationTraceJob (String name){
		super(name);
		this.jobName = name;
		this.outputFiles = new LinkedList<IFile> ();
	}
	
	private SimulationTracer simulationTracer;
	public void setSimulationTracer (SimulationTracer st){
		this.simulationTracer = st;
	}
	private LinkedList<SimulationTraceLog> traceLoggers;
	public void setSimulationTraceLoggers(LinkedList<SimulationTraceLog> slts){
		this.traceLoggers = slts;
	}
	private boolean doGraph = false;
	public void setDoGraph (boolean dg){
		this.doGraph = dg;
	}
	private LinkedList<IFile> outputFiles;
	public void addOutputFile (IFile file){
		this.outputFiles.add(file);
	}
	
	
	private IFile sbrmlFile;
	public void setSbrmlFile (IFile file){
		this.sbrmlFile = file;
	}
	
	private IPath sbrmlPath;
	public void setSbrmlPath (IPath path){
		this.sbrmlPath = path;
	}
	
	private SBAModel sbaModel;
	public void setSbaModel (SBAModel model){
		this.sbaModel = model;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			this.simulationTracer.generateLotsOfTraces(traceLoggers);
			if (this.doGraph) {
				Result result = this.simulationTracer.getSimulationResults();
				// Collect the y information for the graph.
				InfoWithAxes info = new InfoWithAxes();
				// info.setXSeries(Series.create(results[0].getTimePoints(),
				// "Time"));
				List<Series> list = info.getYSeries();

				info.setXSeries(Series.create(result.getTimePoints(), "Time"));
				String[] names = result.getComponentNames();
				for (int i = 0; i < names.length; i++) {
					list.add(Series.create(result.getTimeSeries(i), names[i]));
				}

				info.setShowLegend(true);
				info.setYLabel("Process Count / Variable Value");
			
				/*
				 * Set the graph title to essentially the name of the biopepa
				 * file, if the graph title given is not empty then we appent
				 * that onto the end of the biopepa file name.
				 * 
				 * if (graphTitle.equals("")) {
				 * info.setGraphTitle(path.lastSegment()); } else {
				 * info.setGraphTitle(path.lastSegment() + " - " + graphTitle);
				 * }
				 */
				final IChart chart = Plotting.getPlottingTools().createTimeSeriesChart(info);

				class Semantic implements ISemanticElement {

					public String getDescription(String format) {
						return "Trace Generation Simulation";
					}

					public Object getAdapter(Class arg0) {
						// TODO Auto-generated method stub
						return null;
					}

				}

				// ResultsAdapter ra = new ResultsAdapter(resultsArray);
				chart.setSemanticElement(new Semantic());

				Runnable runnable = new Runnable() {
					public void run() {
						PlotViewPlugin.getDefault().reveal(chart);
					}
				};
				Display.getDefault().syncExec(runnable);
			}
			
			if (this.sbrmlFile != null && this.sbaModel != null){
				Result results = this.simulationTracer.getSimulationResults();
				String filePath = this.sbrmlFile.getLocation().toOSString();
				SBRMLResultExport sbrmlExport = new SBRMLResultExport(this.sbaModel);
				String modelName = this.jobName;
				sbrmlExport.setModelName(modelName);
				sbrmlExport.exportResults(filePath, results);
				this.sbrmlFile.refreshLocal(0, null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, BioPEPAPlugin.PLUGIN_ID, e.getMessage());
		}
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		} else {
			try {
				for (IFile outputFile : this.outputFiles){
					if (outputFile != null) {
						outputFile.refreshLocal(0, null);
					}
				}
				return Status.OK_STATUS;
			} catch (CoreException e) {
				e.printStackTrace();
				return new Status(IStatus.ERROR, BioPEPAPlugin.PLUGIN_ID, e.getMessage());
			}
		}
	}
}
