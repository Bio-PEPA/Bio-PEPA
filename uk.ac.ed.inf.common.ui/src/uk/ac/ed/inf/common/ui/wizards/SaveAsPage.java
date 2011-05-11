/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.wizards;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

/**
 * Create a "Save as..." page which validates the given extension
 * @author Mirco
 *
 */
public class SaveAsPage extends WizardNewFileCreationPage {
	
	protected String extension;
	
	public SaveAsPage(String pageName, IStructuredSelection selection, String extension) {
		super(pageName, selection);
		if (extension == null) {
			throw new NullPointerException("Extension cannot be null");
		}
		this.extension = extension;
		
	}
	
	public String getExtension() {
		return extension;
	}
	
	protected boolean validatePage() {
		if (!super.validatePage())
			return false;
		/* Check extension */
		Path path = new Path(getFileName());
		if (path.getFileExtension() == null || path.getFileExtension().compareToIgnoreCase(
				extension) != 0) {
			this.setErrorMessage("Wrong extension. It must be a ."
					+ extension + " file");
			return false;
		} else {
			this.setMessage(null);
			return true;
		}
	}

}


