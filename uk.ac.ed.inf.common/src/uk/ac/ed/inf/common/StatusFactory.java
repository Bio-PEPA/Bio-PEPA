/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Creates Status objects for this plugin.
 * 
 * @author mtribast
 * 
 */
public class StatusFactory {

	private static final String CANNOT_CREATE_MESSAGE = "Cannot obtain result folder.";

	private static final String CANNOT_ROLLBACK_MESSAGE = "Cannot delete temporary resources.";

	private static final int CANNOT_CREATE_RESULT_FOLDER = 10;

	private static final int CANNOT_ROLLBACK = 11;

	public static Status newCannotObtainResultFolder(IPath path, Throwable th) {
		Status status = new Status(IStatus.ERROR, CommonPlugin.PLUGIN_ID,
				CANNOT_CREATE_RESULT_FOLDER, CANNOT_CREATE_MESSAGE, th);
		return status;
	}

	public static Status newCannotRollback(IPath path, Throwable th) {
		Status status = new Status(IStatus.ERROR, CommonPlugin.PLUGIN_ID,
				CANNOT_ROLLBACK, CANNOT_ROLLBACK_MESSAGE, th);
		return status;
	}

}
