/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotting;

/**
 * The in-memory representation of a chart. An instance of this interface may be
 * used to display the chart on screen or the serialise the metadata to disk. A
 * chart may be associated to a semantic element, i.e. an object from which the
 * data originated.
 * 
 * @author mtribast
 * 
 */
public interface IChart {

	/**
	 * The semantic element, or <code>null</code>
	 * 
	 * @return the semantic element, or <code>null</code>
	 */
	public ISemanticElement resolveSemanticElement();

	/**
	 * Sets the sematic element for this chart. Any previous settings are simply
	 * discarded.
	 * 
	 * @param element
	 */
	public void setSemanticElement(ISemanticElement element);

}
