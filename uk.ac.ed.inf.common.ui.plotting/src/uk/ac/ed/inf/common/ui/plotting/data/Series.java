/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/

package uk.ac.ed.inf.common.ui.plotting.data;

/**
 * A series of data to be plotted. It has a string describing the series and an
 * array of doubles representing the values of the series. A factory method is
 * used to create a new series of data.
 * 
 * @author mtribast
 * 
 */
public class Series {

	private String label; // the label

	private double[] values = new double[0]; // the actual data

	/**
	 * Creates a new series. This method will perform a copy of the array.
	 * 
	 * @param values the values of the series
	 * @param label a description, which may be used in legends.
	 * @return the new series
	 * @throws NullPointerException if values or label is null.
	 */
	public static Series create(double[] values, String label) {
		if (values == null || label == null)
			throw new NullPointerException();
		return new Series(values, label);
	}

	Series(double[] values, String label) {
		this.values = new double[values.length];
		System.arraycopy(values, 0, this.values, 0, values.length);
		this.label = label;
	}
	
	/**
	 * Returns the live array of values of this series.
	 * @return the live array of values.
	 */
	public double[] getValues() {
		return values;
	}
	
	/**
	 * The description of this series.
	 * @return
	 */
	public String getLabel() {
		return this.label;
	}

}
