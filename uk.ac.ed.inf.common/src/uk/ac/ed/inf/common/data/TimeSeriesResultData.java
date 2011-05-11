/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.data;

import java.util.ArrayList;
import java.util.List;


/**
 * Temporary class returning all the results from an SRMC time-series analysis.
 * 
 * @author mtribast
 * 
 */
public class TimeSeriesResultData implements IResultData {
	
	private double[] fTimeAxis = null;

	/* each element of the list is an experiment */
	private List<double[]> fExperiments = new ArrayList<double[]>();

	public TimeSeriesResultData(double[] timeAxis) {
		fTimeAxis = timeAxis;
	}
	
	public void addExperiment(double[] experiment) {
		fExperiments.add(experiment);
	}

	public List<double[]> getExperiments() {
		return fExperiments;
	}
	
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (int time = 0; time < fTimeAxis.length; time++) {
			buf.append(fTimeAxis[time] + " ");
			for (int exp = 0; exp < fExperiments.size(); exp++) {
				buf.append(fExperiments.get(exp)[time] + " ");
			}
			buf.append("\n");
		}
		return buf.toString();
	}

	public int getNumberOfInstances() {
		return fExperiments.size();
	}

	public double[] getTimeSeries() {
		return fTimeAxis;
	}

	public int getType() {
		return IResultData.TIME_SERIES;
	}

	public double[] getValues(int seriesIndex) {
		return fExperiments.get(seriesIndex);
	}

}
