/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotting.internal;

import java.util.HashMap;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.layout.Legend;

import uk.ac.ed.inf.common.ui.plotting.data.AbstractGraphInfo;

/**
 * Infrastructure for a generic 2D graph builder.
 * 
 * @author mtribast
 * 
 */
public abstract class Generic2DGraph {

	protected AbstractGraphInfo info;

	protected Chart chart;

	protected HashMap<OptionKind, String> options = createDefaultOptionMap();
	
	private static HashMap<OptionKind, String> createDefaultOptionMap() {
		HashMap<OptionKind, String> map = new HashMap<OptionKind, String>();
		map.put(OptionKind.EXPLOSION, "5");
		map.put(OptionKind.FONT_NAME, "Arial");
		map.put(OptionKind.TITLE_FONT_SIZE, "14");
		map.put(OptionKind.AXIS_LABEL_FONT_SIZE, "12");
		map.put(OptionKind.LEGEND_FONT_SIZE, "12");
		map.put(OptionKind.AXIS_TICK_FONT_SIZE, "10");
		map.put(OptionKind.X_AXIS_ROTATION, "90");
		return map;
	}

	public Generic2DGraph(AbstractGraphInfo info, Chart chart) {
		if (info == null)
			throw new NullPointerException();
		this.info = info;
		this.chart = chart;
	}

	public final Chart createChart() {
		chart.getTitle().getLabel().getCaption().setValue(info.getGraphTitle());
		chart.getTitle().getLabel().getCaption().getFont().setBold(true);
		chart.getTitle().getLabel().getCaption().getFont().setSize(
				Float.parseFloat(options.get(OptionKind.TITLE_FONT_SIZE)));
		ChartDimension dimension = info.isHas3DEffect() ? ChartDimension.TWO_DIMENSIONAL_WITH_DEPTH_LITERAL
				: ChartDimension.TWO_DIMENSIONAL_LITERAL;
		chart.setDimension(dimension);
		
		handleLegend(chart.getLegend());
		handleChart(chart);
		return chart;
	}
	
	protected abstract void handleChart(Chart chart);

	protected void handleLegend(Legend legend) {
		legend.setItemType(LegendItemType.CATEGORIES_LITERAL);
		legend.setVisible(info.isShowLegend());
		legend.getText().getFont().setSize(Float.parseFloat(options.get(OptionKind.LEGEND_FONT_SIZE)));
	}

}
