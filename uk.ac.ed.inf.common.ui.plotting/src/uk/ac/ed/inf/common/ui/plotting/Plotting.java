/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotting;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import uk.ac.ed.inf.common.ui.plotting.internal.PlottingTools;
import uk.ac.ed.inf.common.ui.plotting.internal.PlottingToolsUI;

/**
 * The activator class controls the plug-in life cycle
 */
public class Plotting extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "uk.ac.ed.inf.common.ui.charting";

	// The shared instance
	private static Plotting plugin;

	private static IPlottingTools plottingTools = new PlottingTools();
	
	private static IPlottingToolsUI plottingToolsUI = new PlottingToolsUI();
	/**
	 * The main entry point to this plug-in's services. Returns the singleton
	 * instance of plotting tool
	 * 
	 * @return the plotting tools.
	 */
	public static IPlottingTools getPlottingTools() {
		return plottingTools;
	}
	
	public static IPlottingToolsUI getPlottingToolsUI() {
		return plottingToolsUI;
	}

	/**
	 * The constructor
	 */
	public Plotting() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Plotting getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
