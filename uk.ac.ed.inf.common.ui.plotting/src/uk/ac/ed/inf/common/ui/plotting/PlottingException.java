/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotting;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

@SuppressWarnings("serial")
public class PlottingException extends CoreException {
	
	public PlottingException(IStatus status) {
		super(status);
	}
	public PlottingException(int severity, String message) {
		super(new Status(severity, Plotting.PLUGIN_ID, message));
	}

}
