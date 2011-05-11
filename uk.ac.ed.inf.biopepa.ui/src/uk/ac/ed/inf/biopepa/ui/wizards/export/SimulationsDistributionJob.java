package uk.ac.ed.inf.biopepa.ui.wizards.export;

import java.util.List;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer;
import uk.ac.ed.inf.biopepa.ui.BioPEPAPlugin;
import uk.ac.ed.inf.biopepa.ui.ProgressMonitorImpl;
import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotting.ISemanticElement;
import uk.ac.ed.inf.common.ui.plotting.Plotting;
import uk.ac.ed.inf.common.ui.plotting.data.InfoWithAxes;
import uk.ac.ed.inf.common.ui.plotting.data.Series;
import uk.ac.ed.inf.common.ui.plotview.PlotViewPlugin;
import uk.ac.ed.inf.biopepa.core.interfaces.ProgressMonitor;


public class SimulationsDistributionJob extends Job {
	public SimulationsDistributionJob(String name) {
		super(name);
	}

	private SimulationTracer simulationTracer;
	public void setSimulationTracer (SimulationTracer st){
		this.simulationTracer = st;
	}
	
	private String targetComp;
	public void setTargetComp(String compName){
		this.targetComp = compName;
	}
	private Integer targetValue;
	public void setTargetValue(Integer value){
		this.targetValue = value;
	}
	private int replications = 100;
	public void setReplications (int reps){
		this.replications = reps;
	}
	private boolean doGraph = true;
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			ProgressMonitor progressMonitor =
				   (monitor == null ? null : 
					   new ProgressMonitorImpl("", monitor));
			this.simulationTracer.calculateDistribution(targetComp,
					targetValue, replications, progressMonitor);
			double [] timepoints = this.simulationTracer.getDistributionTimePoints();
			double [] cdfPoints = this.simulationTracer.getDistributionCdf();
			double [] pdfPoints = this.simulationTracer.getDistributionPdf();
			if (this.doGraph) {
				
				// Result result = this.simulationTracer.getSimulationResults();
				// Collect the y information for the graph.
				InfoWithAxes info = new InfoWithAxes();
				List<Series> list = info.getYSeries();

				info.setXSeries(Series.create(timepoints, "Time"));
				list.add(Series.create(cdfPoints, "cdf"));
				list.add(Series.create(pdfPoints, "pdf"));
				
				info.setShowLegend(true);
				info.setYLabel("Percentage");
			
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
						return "Simulations Distribution";
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
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, BioPEPAPlugin.PLUGIN_ID, e.getMessage());
		}
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		} else {
			return Status.OK_STATUS;
		}
	}
}
