/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import uk.ac.ed.inf.biopepa.ui.*;
import uk.ac.ed.inf.biopepa.ui.editors.BioPEPAEditor;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAListener;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;

public abstract class AbstractAction implements IEditorActionDelegate,
		BioPEPAListener {

	protected BioPEPAModel model;
	protected IAction action = null;
	protected Shell activeShell = null;
	protected IEditorPart bioPEPAEditor;

	public final void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (model != null)
			model.removeListener(this);
		// System.err.println((action == null) + ":" + (targetEditor == null));
		this.action = action;
		if (action == null || targetEditor == null)
			return;
		bioPEPAEditor = targetEditor;
		model = ((BioPEPAEditor) targetEditor).getModel();
		model.addListener(this);
		this.action = action;
		// FIXME Check against null
		activeShell = targetEditor.getEditorSite().getShell();
		checkStatus();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public final void modelChanged(BioPEPAEvent event) {
		checkStatus();
	}

	/**
	 * Override if you need to perform more checks for the concrete action to be
	 * applicable
	 */
	protected void checkStatus() {
		if (action != null)
			action.setEnabled(!model.errorsPresent());
	}
}
