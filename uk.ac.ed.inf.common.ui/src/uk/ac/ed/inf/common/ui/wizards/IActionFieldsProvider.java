/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.wizards;

import org.eclipse.swt.widgets.Text;

public interface IActionFieldsProvider {
	
	public static final IActionFieldsProvider DO_NOTHING_PROVIDER = new IActionFieldsProvider() {

		public void setSourceActionControl(Text sourceActionControl) {
		}

		public void setTargetActionControl(Text targetActionControl) {
		}

		public void setComponentNameControl(Text componentNameControl) {
		}
		
	};
	public void setSourceActionControl(Text sourceActionControl);
	
	public void setTargetActionControl(Text targetActionControl);
	
	public void setComponentNameControl(Text componentNameControl);
}
