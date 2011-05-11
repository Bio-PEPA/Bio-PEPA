/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotview;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import uk.ac.ed.inf.common.ui.plotting.IChart;
import uk.ac.ed.inf.common.ui.plotview.views.PlotView;

/**
 * The activator class controls the plug-in life cycle
 */
public class PlotViewPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "uk.ac.ed.inf.common.ui.plotview";

	// The shared instance
	private static PlotViewPlugin plugin;

	/**
	 * The constructor
	 */
	public PlotViewPlugin() {
	}

	/**
	 * Wraps and logs an exception generated elsewhere
	 * 
	 * @param e
	 * @return
	 */
	public static IStatus wrapException(String message, Exception e) {
		IStatus status = new Status(IStatus.ERROR, PLUGIN_ID, message, e);
		plugin.getLog().log(status);
		return status;
	}

	/**
	 * Shows the chart in the Plot View
	 * 
	 * @param chart
	 */
	public void reveal(final IChart chart) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {

			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				if (window == null)
					return; // no active workbench window
				PlotView plotView = null;
				try {
					plotView = (PlotView) window.getActivePage().showView(
							PlotView.ID);
				} catch (PartInitException e) {
					PlotViewPlugin.getDefault().getLog().log(e.getStatus());
				}
				plotView.reveal(chart);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static PlotViewPlugin getDefault() {
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
