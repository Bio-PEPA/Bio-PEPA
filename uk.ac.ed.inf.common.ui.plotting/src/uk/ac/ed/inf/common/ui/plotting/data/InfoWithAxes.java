/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/

package uk.ac.ed.inf.common.ui.plotting.data;

import java.util.ArrayList;
import java.util.List;



/**
 * Graph information for charts with axes, providing a data set for the x axis and a list
 * of data sets for the y axis.
 * @author mtribast
 *
 */
public class InfoWithAxes extends AbstractGraphInfo {
	
	private String yLabel = EMPTY;
	
	private Series xSeries = null;

	private ArrayList<Series> series = 
		new ArrayList<Series>();
	
	/**
	 * The only series for the x axis. Graphs with axes share
	 * the same x data set. The axis label will be taken from {@link Series#getLabel()}
	 * @return
	 */
	public Series getXSeries() {
		return xSeries;
	}
	
	/**
	 * Sets the new series for the x axis.
	 * @param series
	 */
	public void setXSeries(Series series) {
		xSeries = series;
	}

	/**
	 * The label for the Y axis
	 * @return
	 */
	public String getYLabel() {
		return yLabel;
	}
	
	/**
	 * Sets the label for the Y axis
	 * @param axis
	 */
	public void setYLabel(String axis) {
		yLabel = axis;
	}

	/**
	 * Returns the live list of series for the Y axis.
	 * @return
	 */
	public List<Series> getYSeries() {
		return this.series;
	}

}
