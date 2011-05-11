/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/

package uk.ac.ed.inf.common.ui.plotting.internal;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.factory.RunTimeContext;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.Serializer;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.model.impl.SerializerImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotting.IPlottingTools;
import uk.ac.ed.inf.common.ui.plotting.ISemanticElement;
import uk.ac.ed.inf.common.ui.plotting.Plotting;
import uk.ac.ed.inf.common.ui.plotting.PlottingException;
import uk.ac.ed.inf.common.ui.plotting.data.InfoWithAxes;
import uk.ac.ed.inf.common.ui.plotting.data.InfoWithoutAxes;

/**
 * @author Mirco
 * 
 */
public class PlottingTools implements IPlottingTools {

	public IChart createBarChart(InfoWithAxes info) {
		BarChartBuilder builder = new BarChartBuilder(info);
		CommonChart chart = new CommonChart(builder.createChart());
		return chart;
	}

	public IChart createPieChart(InfoWithoutAxes info) {
		PieChartBuilder builder = new PieChartBuilder(info);
		CommonChart chart = new CommonChart(builder.createChart());
		return chart;
	}

	public IChart createTimeSeriesChart(InfoWithAxes info) {
		LineChartBuilder builder = new LineChartBuilder(info);
		CommonChart chart = new CommonChart(builder.createChart());
		return chart;
	}

	public void convertToPNG(IChart chart, int width, int height, int dpi,
			String filePath) throws PlottingException {

		if (chart == null || filePath == null)
			throw new NullPointerException();

		Chart birtChart = ((CommonChart) chart).getBirtChart();
		PluginSettings ps = PluginSettings.instance();

		try {
			IDeviceRenderer idr = ps.getDevice("dv.PNG");
			RunTimeContext rtc = new RunTimeContext();
			// rtc.setULocale(ULocale.getDefault());
			Generator gr = Generator.instance();
			GeneratedChartState gcs = null;
			// Set the chart size
			Bounds bo = BoundsImpl.create(0, 0, width, height);
			gcs = gr.build(idr.getDisplayServer(), birtChart, bo, null, rtc,
					null);
			// Specify the file to write to.
			idr.setProperty(IDeviceRenderer.FILE_IDENTIFIER, filePath); //$NON-NLS-1$
			idr.setProperty(IDeviceRenderer.DPI_RESOLUTION, dpi);
			// generate the chart
			gr.render(idr, gcs);
		} catch (ChartException ce) {
			throw new PlottingException(IStatus.ERROR, ce.getMessage());
		}

	}

	public void write(IChart chart, String path) throws PlottingException {
		Serializer serialiser = SerializerImpl.instance();
		Chart birtChart = ((CommonChart) chart).getBirtChart();

		try {
			serialiser.write(birtChart, new BufferedOutputStream(
					new FileOutputStream(path)));
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, Plotting.PLUGIN_ID,
					"Serialisation error", e);
			throw new PlottingException(status);
		}

	}

	public byte[] convertToCSV(IChart chart) throws IOException {
		Chart birtChart = ((CommonChart)chart).getBirtChart();
		ICSVExporter exporter = null;
		if (birtChart instanceof ChartWithAxes)
			exporter =  new ChartWithAxesCSVExporter((ChartWithAxes) birtChart);
		else
			exporter = new ChartWithoutAxesCSVExporter((ChartWithoutAxes) birtChart);
		ISemanticElement se = chart.resolveSemanticElement();
		if(se != null)
			exporter.setHeader(se.getDescription(ISemanticElement.CSV_FORMAT));
		return exporter.getCSV();
	}

}
