/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import uk.ac.ed.inf.biopepa.ui.wizards.export.ExportWizard;

public class ExportAction extends AbstractAction {

	public void run(IAction action) {
		WizardDialog dialog = null;
		try {
			ExportWizard wizard = new ExportWizard(model);
			dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
			dialog.open();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
