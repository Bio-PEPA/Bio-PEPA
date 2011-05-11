/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
/* LineChartBuilder.java
 * 
 * created by mtribast on 21 Feb 2008
 *
 */
package uk.ac.ed.inf.common.ui.plotting.internal;

import java.util.ArrayList;
import java.util.Formatter;

import org.eclipse.birt.chart.extension.datafeed.StockEntry;
import org.eclipse.birt.chart.model.attribute.ColorDefinition;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.attribute.LineAttributes;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.Marker;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.StockDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.StockDataSetImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.StockSeries;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.StockSeriesImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

import uk.ac.ed.inf.common.ui.plotting.data.ConfidenceSeries;
import uk.ac.ed.inf.common.ui.plotting.data.InfoWithAxes;

/**
 * @author mtribast
 * @author ajduguid
 * 
 */
public class LineChartBuilder extends ChartWithAxesBuilder {

	private static ColorDefinition[] COLORS;
	private static LineStyle[] LINES;

	// Set to mimic JFreeChart
	static {
		ArrayList<ColorDefinition> colors = new ArrayList<ColorDefinition>();
		colors.add(ColorDefinitionImpl.create(255, 0, 0));
		colors.add(ColorDefinitionImpl.create(0, 0, 255));
		colors.add(ColorDefinitionImpl.create(0, 255, 0));
		colors.add(ColorDefinitionImpl.create(255, 255, 0));
		colors.add(ColorDefinitionImpl.create(255, 200, 0));
		colors.add(ColorDefinitionImpl.create(255, 0, 255));
		colors.add(ColorDefinitionImpl.create(0, 255, 255));
		colors.add(ColorDefinitionImpl.create(255, 175, 175));
		colors.add(ColorDefinitionImpl.create(128, 128, 128));
		colors.add(ColorDefinitionImpl.create(0, 0, 0));
		colors.add(ColorDefinitionImpl.create(192, 0, 0));
		colors.add(ColorDefinitionImpl.create(0, 0, 192));
		colors.add(ColorDefinitionImpl.create(0, 192, 0));
		colors.add(ColorDefinitionImpl.create(192, 192, 0));
		colors.add(ColorDefinitionImpl.create(192, 0, 192));
		colors.add(ColorDefinitionImpl.create(0, 192, 192));
		colors.add(ColorDefinitionImpl.create(64, 64, 64));
		colors.add(ColorDefinitionImpl.create(255, 64, 64));
		colors.add(ColorDefinitionImpl.create(64, 64, 255));
		colors.add(ColorDefinitionImpl.create(64, 255, 64));
		COLORS = colors.toArray(new ColorDefinition[colors.size()]);
		LINES = new LineStyle[] { LineStyle.SOLID_LITERAL,
				LineStyle.DASHED_LITERAL, LineStyle.DOTTED_LITERAL };
	}

	public LineChartBuilder(InfoWithAxes info) {
		super(info, false);
	}

	@Override
	protected void buildXSeries() {
		double[] xDoubles = ((InfoWithAxes) info).getXSeries().getValues();
		NumberDataSet xValues = NumberDataSetImpl.create(xDoubles);
		Series xSeries = SeriesImpl.create();
		xSeries.setDataSet(xValues);
		// String key = "xValues";
		// provider.provide(key, convert(xDoubles));
		// Test for data definitions
		// xSeries.getDataDefinition().add(QueryImpl.create(key));
		// Apply the color palette
		SeriesDefinition sdX = SeriesDefinitionImpl.create();
		// sdX.getSeriesPalette().shift(0);
		xAxis.getSeriesDefinitions().add(sdX);
		// System.err.println("Added x:" + sdX);
		// sdX.getQuery().setDefinition("xaxis");
		sdX.getSeries().add(xSeries);
	}

	@Override
	protected void buildYSeries() {
		// String keyPrefix = "YSeries_";
		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		yAxis.getSeriesDefinitions().add(sdY);
		int i = 0, j = 0;
		for (uk.ac.ed.inf.common.ui.plotting.data.Series series : ((InfoWithAxes) info)
				.getYSeries()) {
			double[] values = series.getValues();
			LineSeries ls = createLineSeries(values);
			ls.setSeriesIdentifier(series.getLabel());
			setAttibutes(ls, i, j);
			sdY.getSeries().add(ls);
			if (series instanceof ConfidenceSeries) {
				this.chart.setType("Stock Chart");
				this.chart.setSubType("Standard Stock Chart");
				double[] radii = ((ConfidenceSeries) series).getRadii();
				StockSeries ss = createStockSeries(values, radii);
				String id = new Formatter().format("%2.1f%% c.i.",
						((ConfidenceSeries) series).getConfidenceLevel() * 100)
						.toString();
				ss.setSeriesIdentifier(id);
				setAttibutes(ss, i, j);
				sdY.getSeries().add(ss);
			}
			i++;
			j++;
		}
	}

	private void setAttibutes(LineSeries series, int colorIndex, int lineIndex) {
		LineAttributes la = series.getLineAttributes();
		la.setColor((ColorDefinition) EcoreUtil.copy(COLORS[colorIndex
				% COLORS.length]));
		la.setStyle(LINES[lineIndex % LINES.length]);
		la.setThickness(2);
	}

	private void setAttibutes(StockSeries series, int colorIndex, int lineIndex) {
		LineAttributes la = series.getLineAttributes();
		la.setColor((ColorDefinition) EcoreUtil.copy(COLORS[colorIndex
				% COLORS.length]));
		la.setStyle(LINES[lineIndex % LINES.length]);
		la.setThickness(2);
	}

	private StockSeries createStockSeries(double[] averages, double[] radii) {
		StockEntry[] entries = new StockEntry[averages.length];
		for (int i = 0; i < entries.length; i++) {
			double down = averages[i] - radii[i];
			double up = averages[i] + radii[i];
			entries[i] = new StockEntry(down, down, up, up);
		}
		StockDataSet stockValues = StockDataSetImpl.create(entries);
		StockSeries ss = (StockSeries) StockSeriesImpl.create();
		ss.setDataSet(stockValues);
		ss.getLabel().setVisible(false);
		ss.setStickLength(1);
		ss.setShowAsBarStick(true);
		return ss;
	}

	private LineSeries createLineSeries(double[] values) {
		NumberDataSet orthohonalValuesDataSet = NumberDataSetImpl
				.create(values);
		LineSeries ls = (LineSeries) LineSeriesImpl.create();
		ls.setDataSet(orthohonalValuesDataSet);
		for (Object o : ls.getMarkers()) {
			((Marker) o).setVisible(info.isShowMarkers());
		}
		ls.getLabel().setVisible(false);
		return ls;
	}

	@Override
	protected void handleLegend(Legend legend) {
		super.handleLegend(legend);
		legend.setItemType(LegendItemType.SERIES_LITERAL);
	}

}
