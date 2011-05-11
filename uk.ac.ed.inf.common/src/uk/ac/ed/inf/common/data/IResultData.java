/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.data;

/**
 * Result data model for analyses of SRMC description.
 * 
 * @author mtribast
 *
 */
public interface IResultData {
	
	/**
	 * Type for results that can be represented by a point.
	 * This includes, for instance, steady-state measures such 
	 * as throughput, utilisation, and so on.
	 * 
	 */
	public static final int POINT = 1;
	
	/**
	 * This is a result obtained by time-series analysis, such as
	 * passage-time analysis, stochastic simulation, or ODE analysis.
	 * 
	 */
	public static final int TIME_SERIES = 2;
	
	/**
	 * Gets the number of intermediate PEPA models which have
	 * been analysed for these result data. This determines the total
	 * number of values available ({@link #getValues(int)}.
	 * 
	 * @return the number of instances used to analyse this model.
	 */
	public int getNumberOfInstances();
	
	/**
	 * Returns the type of the results.
	 * 
	 * @return the type of the results.
	 */
	public int getType();
	
	/**
	 * Returns the time points used in a time-series analysis.
	 * Returns <code>null</code> if the result is of type {@link #POINT}
	 * @return the time points, or <code>null</code>
	 */
	public double[] getTimeSeries();
	
	/**
	 * Returns the results of a given instance of the intermediate
	 * PEPA model. If the result type is {@link #POINT}, then the length
	 * of this array is one.
	 * 
	 * @param seriesIndex the instance of the intermediate PEPA model.
	 * 
	 * @return the result, either a point or an array.
	 * 
	 */
	public double[] getValues(int seriesIndex);

}
