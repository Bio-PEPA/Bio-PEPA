/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewRegistry;

import uk.ac.ed.inf.biopepa.ui.views.BioPEPAInvariantsView;
import uk.ac.ed.inf.common.ui.plotview.views.PlotView;

public class PerspectiveFactory implements IPerspectiveFactory {
	
	public static final String PERSPECTIVE_ID = "uk.ac.ed.inf.biopepa.eclipse.ui.BioPEPAPerspective";
	private static final String SBSI_VISUAL_PROJECTVIEW_ID = "uk.ac.ed.csbe.sbsivisual.projectview";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		IFolderLayout folder = layout.createFolder("uk.ac.ed.inf.biopepa.ui.perspective.left", IPageLayout.LEFT, 0.4f, editorArea);
		IViewRegistry ivr = PlatformUI.getWorkbench().getViewRegistry();
		if(ivr.find(SBSI_VISUAL_PROJECTVIEW_ID) != null)
			folder.addView(SBSI_VISUAL_PROJECTVIEW_ID);
		else
			folder.addView(IPageLayout.ID_RES_NAV);
		folder.addView(IPageLayout.ID_OUTLINE);
		folder = layout.createFolder("uk.ac.ed.inf.biopepa.ui.perspective.bottom", IPageLayout.BOTTOM, 0.6f, "uk.ac.ed.inf.biopepa.ui.perspective.left");
		folder.addView(IPageLayout.ID_PROBLEM_VIEW);
		folder.addView(PlotView.ID);
		folder.addView(BioPEPAInvariantsView.ID);
	}

}
