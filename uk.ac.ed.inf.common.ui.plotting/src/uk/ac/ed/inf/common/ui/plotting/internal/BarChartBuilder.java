/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
/* BarChartBuilder.java
 * 
 * created by mtribast on 21 Feb 2008
 *
 */
package uk.ac.ed.inf.common.ui.plotting.internal;

import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;

import uk.ac.ed.inf.common.ui.plotting.data.InfoWithAxes;

/**
 * @author mtribast
 * 
 */
public class BarChartBuilder extends ChartWithAxesBuilder {

	private SeriesDefinition xSeriesDefinition;

	public BarChartBuilder(InfoWithAxes info) {
		super(info, true);
	}

	@Override
	protected void buildXAxis() {
		super.buildXAxis();
		// overwrites previous settings
		xAxis.getMajorGrid().getLineAttributes().setVisible(false);
		xAxis.getLabel().setVisible(false);
	}

	@Override
	protected void buildXSeries() {
		String[] values = info.getCategories();
		TextDataSet categoryValues = TextDataSetImpl.create(values);
		Series seriesCategory = SeriesImpl.create();

		//String key = "BarChartXSeries";
		//provider.provide(key, values);

		seriesCategory.setDataSet(categoryValues);
		// Test for data definitions
		//	seCategory.getDataDefinition().add(QueryImpl.create(key));
		
		// Apply the color palette
		xSeriesDefinition = SeriesDefinitionImpl.create();
		xSeriesDefinition.getSeriesPalette().shift(1);
		xAxis.getSeriesDefinitions().add(xSeriesDefinition);
		xSeriesDefinition.getSeries().add(seriesCategory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.examples.chart.widget.AbstractChartBuilder#buildYSeries()
	 */
	@Override
	protected void buildYSeries() {

		SeriesDefinition seriesDefinitionY = SeriesDefinitionImpl.create();
		// seriesDefinitionY.setQuery(nextId());
		yAxis.getSeriesDefinitions().add(seriesDefinitionY);

		//int i = 0;
		//String keyPrefix = "BarChartYSeries_";
		for (uk.ac.ed.inf.common.ui.plotting.data.Series series : ((InfoWithAxes) info)
				.getYSeries()) {
			double[] values = series.getValues();
			BarSeries barSeries = createBarSeries(series.getValues());
			{
				//String key = keyPrefix + (i++);
				Double[] recordedValues = new Double[values.length];
				for (int j = 0; j < recordedValues.length; j++)
					recordedValues[j] = values[j];
				//provider.provide(key, recordedValues);
				//barSeries.getDataDefinition().add(QueryImpl.create(key));
			}
			barSeries.setSeriesIdentifier(series.getLabel());
			seriesDefinitionY.getSeriesPalette().shift(1);
			seriesDefinitionY.getSeries().add(barSeries);
		}
	}

	protected void handleLegend(Legend legend) {
		super.handleLegend(legend);
		legend.setItemType(LegendItemType.CATEGORIES_LITERAL);

	}

	private BarSeries createBarSeries(double[] values) {
		NumberDataSet orthoValuesDataSet1 = NumberDataSetImpl.create(values);
		BarSeries series = (BarSeries) BarSeriesImpl.create();
		series.setRiserOutline(null);
		series.setDataSet(orthoValuesDataSet1);
		return series;
	}

}
