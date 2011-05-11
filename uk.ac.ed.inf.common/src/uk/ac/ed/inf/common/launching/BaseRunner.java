/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.launching;

import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import uk.ac.ed.inf.common.StatusFactory;

public abstract class BaseRunner {

	protected Map<String, String> fOptionMap;

	protected ILaunchConfigurationWorkingCopy fCopy;

	protected String fLauncherId;

	protected IFolder fResultFolder;

	/**
	 * Configures a runner with this launcher id
	 * 
	 * @param launcherId
	 * @param optionMap
	 * @throws CoreException
	 */
	public BaseRunner(String launcherId, Map<String, String> optionMap)
			throws CoreException {
		Assert.isNotNull(optionMap);
		Assert.isNotNull(launcherId);
		this.fOptionMap = optionMap;
		this.fLauncherId = launcherId;
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager
				.getLaunchConfigurationType(fLauncherId);
		Assert.isNotNull(type);

		fCopy = type.newInstance(null, fLauncherId + " configuration");

		for (Map.Entry<String, String> entry : fOptionMap.entrySet()) {
			fCopy.setAttribute(entry.getKey(), entry.getValue());
		}

		fResultFolder = LaunchingUtils.getOutputFolder(fCopy);
		if (fResultFolder == null) {
			throw new CoreException(StatusFactory.newCannotObtainResultFolder(
					null, null));
		}

	}

	public final void run(final IProgressMonitor monitor) throws CoreException {

		IResourceRuleFactory factory = ResourcesPlugin.getWorkspace()
				.getRuleFactory();
		ISchedulingRule createRule = factory.createRule(fResultFolder);
		ISchedulingRule modifyRule = factory.modifyRule(fResultFolder);
		ISchedulingRule multiRule = MultiRule.combine(createRule, modifyRule);

		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

			public void run(IProgressMonitor aMonitor) throws CoreException {
				SubMonitor subMonitor = SubMonitor.convert(aMonitor, 100);

				if (fResultFolder.exists()) {
					fResultFolder.delete(true, subMonitor.newChild(15));
				}
				subMonitor.setWorkRemaining(85);
				/* Create folder */
				fResultFolder.create(false, true, subMonitor.newChild(5));
				// consumed 20 so far
				try {
					_run(subMonitor.newChild(75));
				} catch (CoreException e) {
					/* do roll back */
					try {
						fResultFolder.delete(true, subMonitor.newChild(5));
					} catch (CoreException deleteException) {
						throw new CoreException(StatusFactory
								.newCannotRollback(fResultFolder.getFullPath(),
										deleteException));
					}
					throw e;
				} finally {
					
					subMonitor.setWorkRemaining(5);
					_updateFolder(subMonitor.newChild(5));
					
					if (aMonitor != null)
						aMonitor.done();
				
				}

			}

		}, multiRule, IWorkspace.AVOID_UPDATE, monitor);

	}

	protected void _updateFolder(IProgressMonitor monitor) throws CoreException {
		IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				fResultFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}
		};
		myRunnable.run(monitor);

	}

	protected void _updateFolder() throws CoreException {
		_updateFolder(null);
	}

	/**
	 * Runs the analysis. The folder {@link #fResultFolder} is guaranteed to
	 * exist, and a number of scheduling rules are in place to protect it.
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract void _run(SubMonitor monitor) throws CoreException;
}
