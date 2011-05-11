/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.launching;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import uk.ac.ed.inf.common.CommonPlugin;

public class LaunchingUtils {

	private static final IStringVariableManager VAR_MNG = VariablesPlugin
			.getDefault().getStringVariableManager();

	/**
	 * Gets the output folder from the current configuration. Returns
	 * <code>null</code> if a valid folder cannot be calculated
	 * 
	 * @param configuration
	 * @return
	 */
	public static IFolder getOutputFolder(ILaunchConfiguration configuration) {

		String outputDirectory = null;
		try {
			outputDirectory = configuration.getAttribute(
					ILaunchingConstants.SRMC_OUTPUT_DIR, (String) null);
		} catch (CoreException e) {
			return null;
		}

		if (outputDirectory == null)
			return null;

		Path path = new Path(outputDirectory);
		IContainer container = ResourcesPlugin.getWorkspace().getRoot()
				.getContainerForLocation(path);
		if (container == null || container.getType() != IContainer.FOLDER)
			return null;
		return (IFolder) container;
	}

	/**
	 * Prepares command-line arguments for ipc
	 * 
	 * @param config
	 *            launch configuration with parameters
	 * @return the command-line arguments in one string
	 * @throws CoreException
	 */
	public static String prepareCommandLineArguments(ILaunchConfiguration config)
			throws CoreException {

		ArrayList<String> tokens = new ArrayList<String>();

		tokens.add("--start-time");
		tokens.add(config.getAttribute(ILaunchingConstants.IPC_START_TIME,
				ILaunchingConstants.DEFAULT_START_TIME));
		tokens.add("--stop-time");
		tokens.add(config.getAttribute(ILaunchingConstants.IPC_STOP_TIME,
				ILaunchingConstants.DEFAULT_STOP_TIME));
		tokens.add("--time-step");
		tokens.add(config.getAttribute(ILaunchingConstants.IPC_TIME_STEP,
				ILaunchingConstants.DEFAULT_TIME_STEP));

		String[] probeSpecification = null;
		
		String useLocationAware = config.getAttribute(ILaunchingConstants.IPC_USE_LOCATION_AWARE_PROBE, ILaunchingConstants.DEFAULT_LOCATION_AWARE);
		
		if (!Boolean.parseBoolean(useLocationAware)) {
			probeSpecification = createTokensLocationUnaware(config);
		} else {
			probeSpecification = createTokenLocationAware(config);
		}
		for (String t : probeSpecification) {
			tokens.add(t);
		}
		
		tokens.add("--solver");
		tokens.add(config.getAttribute(ILaunchingConstants.IPC_SOLVER_NAME_KEY,
				ILaunchingConstants.DEFAULT_SOLVER));

		if (!Boolean.parseBoolean(config.getAttribute(
				ILaunchingConstants.IPC_STATIC_ANALYSIS,
				ILaunchingConstants.DEFAULT_STATIC_ANALYSIS)))
			tokens.add("--no-static-analysis");

		tokens.add(" "
				+ config.getAttribute(
						ILaunchingConstants.IPC_ADVANCED_ARGUMENTS, ""));

		tokens.add(getModelPath(config));

		StringBuffer buf = new StringBuffer();
		for (String t : tokens)
			buf.append(t + " ");

		return buf.toString();
	}

	private static String[] createTokensLocationUnaware(
			ILaunchConfiguration config) throws CoreException {
		return new String[] {
				"--source="
						+ config.getAttribute(
								ILaunchingConstants.IPC_SOURCE_ACTIONS, ""),
				"--target="
						+ config.getAttribute(
								ILaunchingConstants.IPC_TARGET_ACTIONS, "") };
	}

	private static String[] createTokenLocationAware(ILaunchConfiguration config)
			throws CoreException {
		String probeName = config.getAttribute(ILaunchingConstants.IPC_PROBE_COMPONENT, (String) null);
		String startActions = transform(config.getAttribute(ILaunchingConstants.IPC_SOURCE_ACTIONS,
				(String) null));
		String targetActions = transform(config.getAttribute(ILaunchingConstants.IPC_TARGET_ACTIONS,
				(String) null)); 
		String newToken = "--probe=\"" + probeName + 
				"::(" +  startActions + ":start, "
				+ targetActions + ":stop)\"";
		return new String[] { newToken };
	}
	
	private static String transform(String commaSeparatedValues) {
		String[] actions = commaSeparatedValues.split(",");
		StringBuffer buf = new StringBuffer("(");
		for (int i = 0; i < actions.length-1; i++)
			buf.append(actions[i] + " | ");
		buf.append(actions[actions.length-1] + ")");
		return buf.toString();
	}

	/**
	 * Get the full OS-specific path of the model of this launch configuration
	 * 
	 * @param config
	 * @return <code>null</code> if no path can be obtained
	 * @throws CoreException
	 */
	public static String getModelPath(ILaunchConfiguration config)
			throws CoreException {
		String modelPath = config.getAttribute(
				ILaunchingConstants.IPC_PEPA_FILE_NAME, "");
		return LaunchingUtils.getFullPath(modelPath);
	}

	/**
	 * Resolves the variable of the given text
	 * 
	 * @param text
	 * @return <code>null</code> if path cannot be resolved
	 */
	public static String getFullPath(String text) {
		String path = null;
		try {
			path = VAR_MNG.performStringSubstitution(text).replace('\\', '/');
		} catch (CoreException e) {
		}
		return path;
	}

	/**
	 * Prepares a command for running the file through hydra-s.
	 * <p>
	 * This method is supposed to cope well with cross-platform issues. In
	 * particular, if the underlying OS is Windows, it relies upon cygwin to run
	 * the back-end tool chain.
	 * 
	 * @param file
	 * @return
	 */
	public static String[] prepareCommandLineForHydraS(IFile file) {
		String fileName = getAbsoluteFilename(file);
		ArrayList<String> strings = new ArrayList<String>();
		strings.add("bash");
		strings.add("-c");
		strings.add("hydra-s " + fileName);

		return strings.toArray(new String[strings.size()]);
	}

	/**
	 * Gets the absolute path to this file. It changes location if cygwin is
	 * needed.
	 * 
	 * @param file
	 * @return
	 */
	private static String getAbsoluteFilename(IFile file) {
		IPath location = file.getLocation();
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			location = location.setDevice("/cygdrive/c:");
			return location.toString().replaceFirst("/cygdrive/c:",
					"/cygdrive/c");
		} else {
			return location.toString();
		}
	}

	/**
	 * Prepares a command for running the file through hydra-uniform.
	 * 
	 * @param file
	 * @return
	 */
	public static String[] prepareCommandLineForHydraUniform(IFile file) {
		String fileNameWithDot = file.getLocation().removeFileExtension()
				.toString();
		StringBuffer buf = new StringBuffer();
		buf.append("hydra-uniform ");
		buf.append(fileNameWithDot);
		buf.append(" -cdf");
		return DebugPlugin.parseArguments(buf.toString());

	}

	/**
	 * Synchronously executes a process denoted by the given command-line and
	 * associates it to a launch.
	 * 
	 * @param launch
	 *            the launch of the process
	 * @param commandLine
	 *            the command line arguments
	 * @throws CoreException
	 *             if the process does not quit OK.
	 */
	public static void executeAndWait(ILaunch launch, String[] commandLine)
			throws CoreException {

		Process p = DebugPlugin.exec(commandLine, null);

		RuntimeMonitorWithLock monitor = new RuntimeMonitorWithLock(launch, p,
				commandLine[0], null);

		int exitValue = monitor.waitFor();

		if (exitValue != 0)
			throw new CoreException(new Status(IStatus.ERROR,
					CommonPlugin.PLUGIN_ID, commandLine[0]
							+ " did not quit successfully", null));
	}

}
