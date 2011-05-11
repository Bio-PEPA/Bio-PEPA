/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.editors;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import uk.ac.ed.inf.biopepa.ui.BioPEPAPlugin;
import uk.ac.ed.inf.biopepa.ui.PerspectiveFactory;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;
import uk.ac.ed.inf.biopepa.ui.views.BioPEPAOutline;

public class BioPEPAEditor extends TextEditor {
	
	BioPEPAModel model;
	
	BioPEPAOutline outline = null;
	
	public BioPEPAEditor() {
		super();
		setSourceViewerConfiguration(new BioPEPAViewerConfiguration());
		setDocumentProvider(new BioPEPADocumentProvider());
	}
	
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		/* cache the PEPA model */
		IResource resource = (IResource) input.getAdapter(IResource.class);
		model = BioPEPAPlugin.getDefault().getBioPEPAManager().getModel(resource);
		BioPEPAPlugin.getDefault().getBioPEPAManager().editorOpened(this);
		checkPerspective(site.getPage());
	}
	
	public BioPEPAModel getModel() {
		return model;
	}
	
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if(adapter == IContentOutlinePage.class) {
			if(outline == null || outline.getControl() == null
					|| outline.getControl().isDisposed()) {
				outline = new BioPEPAOutline(model);
				model.addListener(outline);
			}
			return outline;
		}
		return super.getAdapter(adapter);
	}
	
	public void dispose() {
		super.dispose();
		BioPEPAPlugin.getDefault().getBioPEPAManager().editorClosed(this);
	}
	
	/*
	@Override
	protected void createActions() {
		super.createActions();
		Action action = new ContentAssistAction(null, "ContentAssistProposal.", this);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action); 
		markAsStateDependentAction("ContentAssistProposal", true);
	}*/
	
	static void checkPerspective(IWorkbenchPage page) {
		IPerspectiveDescriptor descriptor = page.getPerspective();
		if (descriptor == null)
			return;
		String currentPerspective = descriptor.getId();
		if (!currentPerspective.equals(PerspectiveFactory.PERSPECTIVE_ID)) {
			boolean result = MessageDialog.openQuestion(page.getWorkbenchWindow().getShell(), "Switch to Bio-PEPA Perspective", "This resource is associated with the Bio-PEPA perspective. Would you like to switch to the Bio-PEPA Perspective now?");
			if (result == true) {
				IWorkbench workbench = BioPEPAPlugin.getDefault().getWorkbench();
				page.setPerspective(workbench.getPerspectiveRegistry().findPerspectiveWithId(PerspectiveFactory.PERSPECTIVE_ID));
			}
		}
	}
}
