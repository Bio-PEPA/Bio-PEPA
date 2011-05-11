/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/

package uk.ac.ed.inf.common.ui.plotting.data;

/**
 * An abstract data structure for telling the chart generator how to render the
 * chart. This data is volatile, in that it is used only to initialise the
 * graph. Changes in the state of instances of this class after that the graph
 * is generated will not alter the properties of the graph. To do so, a client
 * must use chart-specific services.
 * <p>
 * This class
 * @author mtribast
 * 
 */
public abstract class AbstractGraphInfo {

	protected static final String EMPTY = "";

	private String graphTitle = EMPTY;

	private boolean showLegend = false;

	private boolean has3DEffect = false;

	private boolean showMarkers = false;

	private String[] categories = new String[0];

	/**
	 * Gets the title of the graph.
	 * 
	 * @return
	 */
	public String getGraphTitle() {
		return graphTitle;
	}

	/**
	 * Sets the title of the graph.
	 * 
	 * @param graphTitle
	 */
	public void setGraphTitle(String graphTitle) {
		this.graphTitle = graphTitle;
	}

	/**
	 * @return <code>true</code> if legend is to be shown
	 */
	public boolean isShowLegend() {
		return showLegend;
	}

	/**
	 * Decides whether to show the legend.
	 * 
	 * @param showLegend
	 */
	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	/**
	 * @return <code>true</code> whether to display graphs with a 3D-like
	 *         effect.
	 */
	public boolean isHas3DEffect() {
		return has3DEffect;
	}

	public void setHas3DEffect(boolean has3DEffect) {
		this.has3DEffect = has3DEffect;
	}

	/**
	 * Returns the array of categories of the data. If this graph information is
	 * used to generate a pie chart, the categories will be interpreted as the
	 * labels of slices. If the graph is a bar-chart, the labels will be used in
	 * the x axis. If the graph is a line chart, these values are simply
	 * ignored.
	 * 
	 * @return the categories of the graph.
	 */
	public String[] getCategories() {
		return categories;
	}

	/**
	 * Set the categories of the graph.
	 * 
	 * @param categories
	 */
	public void setCategories(String[] categories) {
		this.categories = categories;
	}

	/**
	 * Returns whether markers are to be shown in the graph. If
	 * <code>true</code>, the graph may clutter up if the number of elements
	 * of the data set is high. The value is ignored when generating pie-charts
	 * or bar-charts.
	 * 
	 * @return <code>true</code> if markers are to be shown in line charts.
	 */
	public boolean isShowMarkers() {
		return showMarkers;
	}

	/**
	 * Sets the appearence of markers in line charts. It has no effect on other
	 * kinds of graphs.
	 * 
	 * @param showMarkers
	 */
	public void setShowMarkers(boolean showMarkers) {
		this.showMarkers = showMarkers;
	}

}
