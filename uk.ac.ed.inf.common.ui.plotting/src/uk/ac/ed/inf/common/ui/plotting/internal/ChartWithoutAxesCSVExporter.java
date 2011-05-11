/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotting.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.data.SeriesDefinition;

public class ChartWithoutAxesCSVExporter extends AbstractCSVExporter {

	private ChartWithoutAxes fChart;

	public ChartWithoutAxesCSVExporter(ChartWithoutAxes birtChart) {
		fChart = birtChart;
	}

	@Override
	protected void handleChart(ByteArrayOutputStream outputStream)
			throws IOException {
		SeriesDefinition xSeriesDef = (SeriesDefinition) fChart
				.getSeriesDefinitions().get(0);
		Series xSeries = (Series) xSeriesDef.getSeries().get(0);
		Object xValues = xSeries.getDataSet().getValues();
		int numOfColumns = -1;
		if (xValues instanceof double[])
			numOfColumns = ((double[]) xValues).length;
		else if (xValues instanceof String[])
			numOfColumns = ((String[]) xValues).length;
		else
			throw new IOException("Conversion of " + xValues.getClass()
					+ " not supported");
		for (int c = 0; c < numOfColumns; c++) {
			if (c == 0)
				outputStream.write("# ".getBytes());
			String text = null;
			if (xValues instanceof double[])
				text = Double.toString(((double[]) xValues)[c]);
			else if (xValues instanceof String[])
				text = format(((String[]) xValues)[c]);
			outputStream.write(text.getBytes());
			if (c != numOfColumns - 1)
				outputStream.write(SEP);
		}
		outputStream.write(NEW_LINE);
		SeriesDefinition ySeriesDef = (SeriesDefinition) xSeriesDef
				.getSeriesDefinitions().get(0);
		Series ySeries = (Series) ySeriesDef.getSeries().get(0);
		Object yValues = ySeries.getDataSet().getValues();
		for (int c = 0; c < numOfColumns; c++) {
			String text = null;
			if (yValues instanceof double[])
				text = Double.toString(((double[]) yValues)[c]);
			else if (yValues instanceof String[])
				text = format(((String[]) yValues)[c]);
			outputStream.write(text.getBytes());
			if (c != numOfColumns - 1)
				outputStream.write(SEP);
		}
	}
}
