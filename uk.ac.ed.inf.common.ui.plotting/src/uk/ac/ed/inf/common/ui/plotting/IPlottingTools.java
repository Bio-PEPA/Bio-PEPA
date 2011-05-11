/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/

package uk.ac.ed.inf.common.ui.plotting;

import java.io.IOException;

import uk.ac.ed.inf.common.ui.plotting.data.InfoWithAxes;
import uk.ac.ed.inf.common.ui.plotting.data.InfoWithoutAxes;

/**
 * This interface provides this plugin's main tools for generating charts. All
 * the methods which create charts returns objects with no association with
 * semantic elements.
 * <p>
 * This interface is not intended to be implemented by clients.
 * @author mtribast
 * 
 */
public interface IPlottingTools {

	/**
	 * Creates a pie chart with the information given in an instance of
	 * {@link InfoWithoutAxes}. In particular, such an instance must have set
	 * the categories ({@link InfoWithoutAxes#setCategories(String[])}) and
	 * their corresponding values ({@link InfoWithoutAxes#setValues(double[])}).
	 * The categories will be shown in the chart's legend, whereas the values
	 * will be labels to the pie's slices.
	 * 
	 * @param info
	 * @return the pie chart
	 */
	IChart createPieChart(InfoWithoutAxes info);

	/**
	 * Creates a bar chart with the information given in an instance of
	 * {@link InfoWithAxes}. This supports multi-series data, whose labels will
	 * be shown in the legend. Each series will be plotted using a different
	 * colour form a standard palette. The categories of the info must be set in
	 * order for the chart engine to generate the x axis.
	 * 
	 * @param info
	 * @return
	 */
	IChart createBarChart(InfoWithAxes info);

	/**
	 * Creates a time series chart with the information given in an instance of
	 * {@link InfoWithAxes}. This supports multi-series data, whose labels will
	 * be shown in the legend. Each series will be plotted using a different
	 * colour form a standard palette. The series for the x axis must be set
	 * with
	 * {@link InfoWithAxes#setXSeries(uk.ac.ed.inf.common.ui.plotting.data.Series)} .
	 * Information on categories is ignored.
	 * 
	 * @param info
	 * @return
	 */
	IChart createTimeSeriesChart(InfoWithAxes info);
	
	/**
	 * Converts the chart into a PNG file
	 * @param chart the chart to convert
	 * @param width the width in points
	 * @param height the height in points 
	 * @param filePath the absolute path to the PNG file
	 * @throws PlottingException
	 */
	void convertToPNG(IChart chart, int width, int height, int dpi, String filePath) throws PlottingException;
	
	/**
	 * Converts the chart's data to a CSV file.
	 * @param chart the chart from which the data are extracted
	 * @return the CSV-formatted array of bytes.
	 * @throws IOException if any error occurs during conversion.
	 */
	byte[] convertToCSV(IChart chart) throws IOException;
	
	
	
}