/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.jobs;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.IProgressConstants;

import uk.ac.ed.inf.common.data.IResultData;
import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotting.Plotting;
import uk.ac.ed.inf.common.ui.plotting.data.InfoWithAxes;
import uk.ac.ed.inf.common.ui.plotting.data.Series;

/**
 * This job can be used to schedule analyses in the background and collect
 * results to be presented to the Graph View.
 * <p>
 * Clients implement the abstract method
 * {@link #createResultData(IProgressMonitor)} to run the analysis.
 * 
 * @author mtribast
 * 
 */
public abstract class ResultCollectorJob extends WorkspaceJob {

	private Action action;

	public ResultCollectorJob(String name) {
		super(name);
	}

	@Override
	public final IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
		/*
		 * job's results contains the core exception's status throw by the
		 * runner
		 */
		action = null;
		
		IResultData data = createResultData(monitor);
		
		action = createOKAction(data);
		action.setText("Show results");
		if (isModal()) {
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					action.run();
				}
			});
		} else {
			setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
			setProperty(IProgressConstants.ACTION_PROPERTY, action);
		}

		/* otherwise, ok is returned */
		return Status.OK_STATUS;

	}

	/**
	 * Clients implement this method to run the analyser.
	 * 
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	protected abstract IResultData createResultData(IProgressMonitor monitor)
			throws CoreException;

	protected Action createOKAction(final IResultData data) {
		
		return new Action("View results") {
			
			public void run() {
				
				InfoWithAxes info = new InfoWithAxes();
				Series x = Series.create(data.getTimeSeries(), "Time");
				info.setXSeries(x);
				for (int s = 0; s <  data.getNumberOfInstances(); s++) {
					Series y = Series.create(data.getValues(s), "Exp. " +(s+1));
					info.getYSeries().add(y);
				}
				info.setYLabel("Probability");
				info.setShowLegend(true);
				info.setShowMarkers(false);
				info.setGraphTitle("Passage-Time Analysis");
				IChart chart = Plotting.getPlottingTools().createTimeSeriesChart(info);
				uk.ac.ed.inf.common.ui.plotview.PlotViewPlugin.getDefault().reveal(
						chart);

			}
		};
	}

	private boolean isModal() {
		Boolean isModal = (Boolean) getProperty(IProgressConstants.PROPERTY_IN_DIALOG);
		if (isModal == null)
			return false;
		return isModal.booleanValue();
	}

}
