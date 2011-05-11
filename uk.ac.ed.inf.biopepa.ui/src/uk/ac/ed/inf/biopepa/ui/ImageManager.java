/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class ImageManager {
	
	private static ImageManager manager = null;
	
	private static final String ICON_PATH = "icons/";
	
	private ImageRegistry registry;
	
	public enum ICONS {
		CLEAR("clear.gif");
		
		String filename;
		
		ICONS(String filename) {this.filename = filename;}
		
		String getFileName() {return filename;}
	}
	
	private ImageManager() {
		this.registry = BioPEPAPlugin.getDefault().getImageRegistry();
	}

	public static ImageManager getInstance() {
		if (manager == null)
			manager = new ImageManager();
		return manager;
	}
	
	public Image getImage(ICONS icon) {
		return getImageDescriptor(icon).createImage();
	}
	
	public ImageDescriptor getImageDescriptor(ICONS icon) {
		ImageDescriptor descriptor = registry.getDescriptor(icon.getFileName());
		if (descriptor == null) {
			descriptor = BioPEPAPlugin.getImageDescriptor(ICON_PATH + icon.getFileName());
			registry.put(icon.getFileName(), descriptor);
		}
		return descriptor;
	}


	public void dispose() {
		registry.dispose();
	}
}
