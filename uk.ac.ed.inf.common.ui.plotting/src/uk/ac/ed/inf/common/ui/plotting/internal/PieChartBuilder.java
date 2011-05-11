/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
/* PieChartBuilder.java
 * 
 * created by mtribast on 21 Feb 2008
 *
 */
package uk.ac.ed.inf.common.ui.plotting.internal;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.birt.chart.model.type.PieSeries;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;

import uk.ac.ed.inf.common.ui.plotting.data.InfoWithoutAxes;

/**
 * @author mtribast
 * 
 */
public class PieChartBuilder extends Generic2DGraph {

	private SeriesDefinition sdX;

	public PieChartBuilder(InfoWithoutAxes info) {
		super(info, ChartWithoutAxesImpl.create());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sct.birt.test.data.Generic2DGraph#handleChart(org.eclipse.birt.chart.model.Chart)
	 */
	@Override
	protected void handleChart(Chart chart) {
		buildXSeries(chart);
		buildYSeries();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.examples.chart.widget.chart.AbstractChartBuilder#buildXSeries()
	 */
	private void buildXSeries(Chart chart) {

		TextDataSet categoryValues = TextDataSetImpl.create(info
				.getCategories());

		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);

		// Apply the color palette
		sdX = SeriesDefinitionImpl.create();
		sdX.getSeriesPalette().shift(1);

		((ChartWithoutAxes) chart).getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.examples.chart.widget.AbstractChartBuilder#buildYSeries()
	 */
	private void buildYSeries() {

		NumberDataSet orthoValuesDataSet = NumberDataSetImpl
				.create(((InfoWithoutAxes) info).getValues());

		PieSeries sePie = (PieSeries) PieSeriesImpl.create();
		sePie.setDataSet(orthoValuesDataSet);
		sePie.setExplosion(Integer.parseInt(options.get(OptionKind.EXPLOSION)));

		SeriesDefinition sdCity = SeriesDefinitionImpl.create();
		sdX.getSeriesDefinitions().add(sdCity);
		sdCity.getSeries().add(sePie);
	}

}
