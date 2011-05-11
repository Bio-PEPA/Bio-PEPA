/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * This class manages image retrieval for the PEPA Eclipse plug-in
 * 
 * @author mtribast
 * 
 */
public class CommonImageManager {

	private static CommonImageManager manager = null;
	
	/* Plug-in relative icon path */
	private static final String ICON_PATH = "icons/";
	
	public static final String EXPORT = "export_wiz.gif";
	
	public static final String INCREASE_THICKNESS = "thickness_increase.gif";
	
	public static final String DECREASE_THICKNESS = "thickness_decrease.gif";
	
	private ImageRegistry registry;

	private CommonImageManager() {
		this.registry = CommonUIPlugin.getDefault().getImageRegistry();
	}

	public static CommonImageManager getInstance() {
		if (manager == null)
			manager = new CommonImageManager();
		return manager;
	}

	public Image getImage(String imageString) {
		return getImageDescriptor(imageString).createImage();
	}
	
	public ImageDescriptor getImageDescriptor(String imageString) {
		ImageDescriptor descriptor = registry.getDescriptor(imageString);
		if (descriptor == null) {
			descriptor = CommonUIPlugin.getImageDescriptor(ICON_PATH + imageString);
			registry.put(imageString, descriptor);
		}
		return descriptor;
		
			
	}
	
	public void dispose() {
		registry.dispose();
	}
}
