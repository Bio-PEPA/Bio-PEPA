/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotting.internal;

import org.eclipse.birt.chart.model.Chart;

import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotting.ISemanticElement;

/**
 * Base implementation does not have any sematic element.
 * 
 * @author mtribast
 *
 */
public class CommonChart implements IChart {
	
	private Chart birtChart;
	
	private ISemanticElement semanticElement = null;
	
	public CommonChart(Chart birtChart) {
		this.birtChart = birtChart;
	}
	
	public Chart getBirtChart() {
		return birtChart;
	}
	
	public ISemanticElement resolveSemanticElement() {
		return semanticElement;
	}

	public void setSemanticElement(ISemanticElement element) {
		// may be null
		this.semanticElement = element;
	}

}
