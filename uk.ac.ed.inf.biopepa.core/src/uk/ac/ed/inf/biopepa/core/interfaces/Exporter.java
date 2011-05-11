/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.interfaces;

import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;

public interface Exporter {

	public String getShortName();

	public String getLongName();

	public String getDescription();

	/**
	 * 
	 * @param model
	 * @throws UnsupportedOperationException
	 *             is thrown if the exporter implementing this interface
	 *             requires a ModelCompiler object rather than an SBAModel.
	 */
	public void setModel(SBAModel model) throws UnsupportedOperationException;

	/**
	 * 
	 * @param compiledModel
	 * @throws UnsupportedOperationException
	 *             is thrown if the exporter implementing this interface
	 *             requires an SBAModel object rather than a ModelCompiler.
	 */
	public void setModel(ModelCompiler compiledModel) throws UnsupportedOperationException;

	/**
	 * 
	 * @return the class of the required data structure for exporting. This will
	 *         currently mean either a ModelCompiler object (obtainable from a
	 *         Model) or an SBAModel object.
	 */
	public Object requiredDataStructure();

	/**
	 * For formats that record the name of the model within their data
	 * structure. If not supplied one potential alternative is the use of the
	 * hashcode of the model (but not enforced).
	 * 
	 * @param modelName
	 */
	public void setName(String modelName);

	public String toString();

	/**
	 * 
	 * @return
	 * @throws UnsupportedOperationException
	 */
	public Object toDataStructure() throws UnsupportedOperationException;

	/**
	 * 
	 * @return the file prefix for this format.
	 */
	public String getExportPrefix();

	/**
	 * 
	 * @return null if there is no issue exporting. A string being returned is
	 *         the reason
	 */
	public String canExport();

}
