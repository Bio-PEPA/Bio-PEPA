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

import org.eclipse.birt.chart.extension.datafeed.StockEntry;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.StockDataSet;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.StockSeries;

public class ChartWithAxesCSVExporter extends AbstractCSVExporter {

	private ChartWithAxes fChart;

	
	public ChartWithAxesCSVExporter(ChartWithAxes birtChart) {
		fChart = birtChart;
	}

	@Override
	protected void handleChart(ByteArrayOutputStream outputStream)
			throws IOException {
		Axis xAxis = fChart.getPrimaryBaseAxes()[0];
		Axis yAxis = fChart.getPrimaryOrthogonalAxis(xAxis);
		SeriesDefinition ySeriesDef = (SeriesDefinition) yAxis
				.getSeriesDefinitions().get(0);
		int numberOfSeries = ySeriesDef.getSeries().size();
		String xAxisLabel = xAxis.getTitle().getCaption().getValue();
		SeriesDefinition xSeriesDef = (SeriesDefinition) xAxis.getSeriesDefinitions().get(0);
		Series xSeries = (Series) xSeriesDef.getSeries().get(0);
		Object xValues = xSeries.getDataSet().getValues();
		int numberOfPoints = -1;

		if (xValues instanceof double[]) {
			numberOfPoints = ((double[]) xValues).length;
		} else if (xValues instanceof String[]) {
			numberOfPoints = ((String[]) xValues).length;
		} else
			throw new IOException("Conversion of " + xValues.getClass()
					+ " not supported");
		outputStream.write(("# " + xAxisLabel).getBytes());
		outputStream.write(SEP);
		for (int i = 0; i < numberOfSeries; i++) {
			String title = format((String) ((Series) ySeriesDef.getSeries().get(i))
					.getSeriesIdentifier());
			outputStream.write(title.getBytes());
			if (i != numberOfSeries - 1)
				outputStream.write(SEP);

		}
		outputStream.write(NEW_LINE);
		for (int p = 0; p < numberOfPoints; p++) {
			String xElem = null;
			if (xValues instanceof double[]) {
				xElem = Double.toString(((double[]) xValues)[p]);
			} else {
				xElem = format(((String[]) xValues)[p]);
			}
			outputStream.write(xElem.getBytes());
			outputStream.write(SEP);
			for (int s = 0; s < numberOfSeries; s++) {

				Series series = (Series) ySeriesDef.getSeries().get(s);
				String yElem = Double.toString(getValueAtTimePoint(series, p));
				outputStream.write(yElem.getBytes());
				if (s != numberOfSeries - 1)
					outputStream.write(SEP);
				
			}
			outputStream.write(NEW_LINE);
		}
	}
	
	private double getValueAtTimePoint(Series series, int timePoint) {
		if (series instanceof LineSeries)
			return ((double[]) series.getDataSet().getValues())[timePoint];
		else if (series instanceof StockSeries){
			StockDataSet set = (StockDataSet) series.getDataSet();
			return ((StockEntry[]) set.getValues())[timePoint].getLow();
		}
		throw new IllegalStateException();
	}

}
