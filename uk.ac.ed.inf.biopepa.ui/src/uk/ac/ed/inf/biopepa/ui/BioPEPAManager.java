/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import uk.ac.ed.inf.biopepa.ui.editors.BioPEPAEditor;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;

public class BioPEPAManager {
	
	public static boolean isValidBioPEPAFile(IResource resource) {		
		if (resource == null)
			return false;
		return (resource.getType() == IResource.FILE && resource.getFileExtension().equals("biopepa"));
	}
	
	private Map<IResource, BioPEPAModel> map = new HashMap<IResource, BioPEPAModel>();
	private Map<BioPEPAModel, Integer> editorCount = new HashMap<BioPEPAModel, Integer>();

	
	private IResourceChangeListener resourceListener = new IResourceChangeListener() {

		public void resourceChanged(IResourceChangeEvent event) {
			try {
				IResourceDelta resourceDelta = event.getDelta();
				if (resourceDelta == null)
					return;

				resourceDelta.accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta)
							throws CoreException {
						if (!isValidBioPEPAFile(delta.getResource())) {
							return true;
						}
						switch (delta.getKind()) {
						case IResourceDelta.ADDED:
							break;
						case IResourceDelta.REMOVED:
							if(map.containsKey(delta.getResource()))
								map.remove(delta.getResource()).dispose();
							break;
						case IResourceDelta.CONTENT:
						case IResourceDelta.CHANGED:
							// only interested in content change (not markers)
							if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
								break;
							if (map.containsKey(delta.getResource()))
								map.get(delta.getResource()).parse();
							break;
						default:
							break;
						}
						return true;
					}
				});
			
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	};

	BioPEPAManager() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.PRE_BUILD | IResourceChangeEvent.PRE_CLOSE);
	}
	
	public void editorOpened(BioPEPAEditor editor) {
		BioPEPAModel model = editor.getModel();
		Integer count = editorCount.get(model);
		if(count == null)
			editorCount.put(model, 1);
		else
			editorCount.put(model, count + 1);
	}

	public void editorClosed(BioPEPAEditor editor) {
		BioPEPAModel model = editor.getModel();
		Integer count = editorCount.get(model);
		if(count == 1) {
			editorCount.remove(model);
			IResource resource = null;
			for(Map.Entry<IResource, BioPEPAModel> me : map.entrySet())
				if(me.getValue() == model) {
					resource = me.getKey();
					break;
				}
			map.remove(resource);
			model.dispose();
		} else {
			editorCount.put(model, count - 1);
		}
	}
	
	public BioPEPAModel getModel(IResource resource) {
		if (resource == null || !resource.exists())
			return null;
		BioPEPAModel model = map.get(resource);
		if (model == null && isValidBioPEPAFile(resource)) {
			try {
				model = new BioPEPAModelImpl(resource);
				map.put(resource, model);
				model.parse();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return model;
	}
	
	public void shutdown() {
		for(BioPEPAModel model : map.values())
			model.dispose();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
	}

}
