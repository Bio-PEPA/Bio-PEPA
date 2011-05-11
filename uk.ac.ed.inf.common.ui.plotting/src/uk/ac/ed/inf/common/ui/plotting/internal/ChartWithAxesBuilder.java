/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
/* ChartWithAxesBuilder.java
 * 
 * created by mtribast on 21 Feb 2008
 *
 */
package uk.ac.ed.inf.common.ui.plotting.internal;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.*;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Grid;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;

import uk.ac.ed.inf.common.ui.plotting.data.InfoWithAxes;

/**
 * @author mtribast
 * @author ajduguid
 * 
 */
public abstract class ChartWithAxesBuilder extends Generic2DGraph {

	protected Axis xAxis;

	protected Axis yAxis;

	protected boolean isXAxisText;
	
	public ChartWithAxesBuilder(InfoWithAxes info, boolean isXAxisText) {
		super(info, ChartWithAxesImpl.create());
		this.isXAxisText = isXAxisText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sct.birt.test.data.Generic2DGraph#handleChart(org.eclipse.birt.chart.model.Chart)
	 */
	@Override
	protected void handleChart(Chart chart) {
		// X
		xAxis = ((ChartWithAxes) chart).getPrimaryBaseAxes()[0];
		buildXAxis();
		buildXSeries();
		// Y
		yAxis = ((ChartWithAxes) chart).getPrimaryOrthogonalAxis(xAxis);
		buildYAxis();
		buildYSeries();
		
		//System.err.println("Associated axis: " + xAxis.getAssociatedAxes().size());
		//System.err.println(yAxis);
		//System.err.println(xAxis.getAssociatedAxes().get(0));
	}

	protected abstract void buildXSeries();

	protected abstract void buildYSeries();

	/**
	 * In particular, sets the type according to the kind of information
	 */
	protected void buildXAxis() {
		// set type
		AxisType type = isXAxisText ? AxisType.TEXT_LITERAL : AxisType.LINEAR_LITERAL;
		double rotation = isXAxisText ? Double.parseDouble(this.options.get(OptionKind.X_AXIS_ROTATION)) : 0;
		xAxis.setType(type);
		xAxis.getLabel().getCaption().getFont().setRotation(rotation);
		
		// set captions
		setCaptions(xAxis, ((InfoWithAxes)info).getXSeries().getLabel());

		// set grid
		setGrid(xAxis, TickStyle.BELOW_LITERAL);

		// set origin
		//xAxis.getOrigin().setType(IntersectionType.VALUE_LITERAL);
	}

	protected void buildYAxis() {
		// set type
		yAxis.setType(AxisType.LINEAR_LITERAL);
		// set caption
		setCaptions(yAxis, ((InfoWithAxes) info).getYLabel());

		// set grid
		setGrid(yAxis, TickStyle.LEFT_LITERAL);

		//yAxis.getOrigin().setType(IntersectionType.VALUE_LITERAL);
	}

	private void setCaptions(Axis axis, String title) {
		// label captions
		axis.getLabel().setVisible(true);
		axis.getLabel().getCaption().getFont().setSize(
				Float.parseFloat(options.get(OptionKind.AXIS_TICK_FONT_SIZE)));
		if (title.length()==0) {
			axis.getTitle().setVisible(false);
		} else {
			axis.getTitle().setVisible(true);
		}
		// caption
		Text titleCaption = axis.getTitle().getCaption();
		titleCaption.setValue(title);
		titleCaption.getFont().setBold(false);
		titleCaption.getFont().setSize(
				Float.parseFloat(options.get(OptionKind.AXIS_LABEL_FONT_SIZE)));
	}

	protected void setGrid(Axis axis, TickStyle style) {
		Grid majorGrid = axis.getMajorGrid();
		majorGrid.setTickStyle(style);
		majorGrid.getLineAttributes().setVisible(true);
		majorGrid.getLineAttributes().setColor(ColorDefinitionImpl.GREY());
		majorGrid.getLineAttributes().setStyle(LineStyle.DASHED_LITERAL);
	}

}
