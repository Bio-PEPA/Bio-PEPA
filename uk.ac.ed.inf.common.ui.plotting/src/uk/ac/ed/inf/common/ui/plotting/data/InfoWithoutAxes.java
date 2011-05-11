/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/

package uk.ac.ed.inf.common.ui.plotting.data;

/**
 * Simple dataset for graph without axes, i.e. a simple bar chart or a
 * pie-chart. The actual kind is specified by {@link AbstractGraphInfo.Kind}.
 * Categories will be used as labels, in legends for pie-charts. If a bar-chart
 * is selected, they will be shown on the axis.
 * 
 * 
 * @author mtribast
 * 
 */
public class InfoWithoutAxes extends AbstractGraphInfo {

	private double[] values = new double[0];

	/**
	 * The values for each category. Thus, it must be of the same lenght as
	 * {@link AbstractGraphInfo#getCategories()}.
	 * 
	 * @return
	 */
	public double[] getValues() {
		return values;
	}
	
	/**
	 * Set the values for each category.
	 * @param values
	 */
	public void setValues(double[] values) {
		this.values = values;
	}

}
