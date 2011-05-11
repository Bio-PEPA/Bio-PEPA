/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.interfaces;

import java.util.Map;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

public interface Result {

	// public void assimilateResult (Result result);

	String[] getActionNames();

	double getActionThroughput(int index);

	String[] getComponentNames();

	Map<String, Number> getModelParameters();

	double getPopulation(int index);

	String getSimulatorName();

	Map<String, Number> getSimulatorParameters();

	double[] getTimePoints();

	double[] getTimeSeries(int index);

	boolean throughputSupported();
	
	void normaliseResult(double [] newTimePoints);
	
	double getSimulationRunTime ();
	void setSimulationRunTime(double timeInSeconds);
	
	/*
	 * Concatenates the given set of results into the
	 * current set. This assumes that both result sets
	 * are for the same set of names (not necessarily the
	 * same model). The given set of results will have time
	 * points beginning at zero, each time point of the given
	 * set will have the end point of the current results added
	 * to them, so that the resulting set of results forms a
	 * continuation of time. In general you would expect that
	 * the initial populations for the given set of results
	 * would equal that of the ending populations for the current
	 * results, but that is not enforced here. 
	 */
	void concatenateResults (Result result) throws BioPEPAException;
}
