/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.interfaces;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.sba.Parameters;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;

/**
 * 
 * @author ajduguid
 * 
 */
public interface Solver {

	String getDescriptiveName();

	Parameters getRequiredParameters();

	String getShortName();

	Result startTimeSeriesAnalysis(SBAModel model, Parameters parameters,
			ProgressMonitor monitor) throws BioPEPAException;

	SolverResponse getResponse(SBAModel model);

	public interface SolverResponse {

		public enum Suitability {
			PERMISSIBLE, WARNING, UNSUITABLE
		}

		Suitability getSuitability();

		String getMessage();
	}
}