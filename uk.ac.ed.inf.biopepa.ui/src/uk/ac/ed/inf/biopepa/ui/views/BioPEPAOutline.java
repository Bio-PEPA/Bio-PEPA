/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.views;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import uk.ac.ed.inf.biopepa.core.sba.OutlineAnalyser;
import uk.ac.ed.inf.biopepa.core.sba.SimpleTree;
import uk.ac.ed.inf.biopepa.ui.BioPEPAEvent;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAListener;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;

public class BioPEPAOutline extends ContentOutlinePage implements
		BioPEPAListener {
	
	private Runnable runnable = new Runnable() {
		public void run() {
			TreeViewer tv = getTreeViewer();
			tv.setInput(bt);
			if(expanded != null)
				tv.setExpandedElements(expanded);
		}		
	};

	public BioPEPAOutline(BioPEPAModel model) {
		this.model = model;
	}


	private class OutlineContentProvider extends ArrayContentProvider implements
			ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			return ((SimpleTree) parentElement).getChildren();
		}

		public Object getParent(Object element) {
			return ((SimpleTree) element).getParent();
		}

		public boolean hasChildren(Object element) {
			return ((SimpleTree) element).getChildren().length != 0;
		}

	}

	private class OutlineLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((SimpleTree) element).getName();
		}
	}

	private BioPEPAModel model;
	private SimpleTree[] bt;
	private Object[] expanded;

	public void modelChanged(BioPEPAEvent event) {
		if (event.getEvent().equals(BioPEPAEvent.Event.PARSED)
		   || event.getEvent().equals(BioPEPAEvent.Event.MODIFIED)) {
			refreshTree();
		} else if (event.getEvent().equals(BioPEPAEvent.Event.EXCEPTION))
			refreshTree();
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		getTreeViewer().setContentProvider(new OutlineContentProvider());
		getTreeViewer().setLabelProvider(new OutlineLabelProvider());
		refreshTree();
	}

	
	
	
	private void refreshTree() {
		OutlineAnalyser outlineanalyser = new OutlineAnalyser();
		this.expanded = null;
		SimpleTree[] newtree = 
				outlineanalyser.createOutlineTree(model.getSBAModel());
		
		/* If the current tree is not null, then we may have some
		 * elements of the tree which are expanded. So it is good
		 * to have them remain expanded even if the tree has been
		 * updated. TODO: please check that this actually works?
		 */
		if(bt != null) {
			// getTreeViewer is only accessible from the UI thread
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					TreeViewer tv = getTreeViewer();
					expanded = tv.getExpandedElements();
				}					
			});
		}
		
	    bt = newtree;
		
		Display.getDefault().asyncExec(runnable);
	}
}
