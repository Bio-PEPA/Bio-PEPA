/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.launching;

import uk.ac.ed.inf.common.CommonPlugin;

/**
 * Enumeration of constant strings which are used by the launching framework of
 * SRMC.
 * 
 * @author mtribast
 * 
 */
public interface ILaunchingConstants {

	/**
	 * Absolute path of the directory where the underlying PEPA files along with
	 * their mappings will be stored. This directory can be mapped to an
	 * IResource, as it is under the workspace root.
	 */
	public static final String SRMC_OUTPUT_DIR = CommonPlugin.PLUGIN_ID
			+ ".output_dir";

	/**
	 * List of args for ipc to perform analysis on the models.
	 */
	public static final String SRMC_IPC_ARGS = CommonPlugin.PLUGIN_ID
			+ ".ipc_args";

	/**
	 * File path of the Srmc Model
	 */
	public static final String SRMC_FILE_PATH = CommonPlugin.PLUGIN_ID
			+ ".file_path";

	/**
	 * Steady-state probability distribution solver name
	 */
	public static final String IPC_SOLVER_NAME_KEY = CommonPlugin.PLUGIN_ID
			+ "." + "solver";

	public static final String IPC_ANALYSIS_TYPE_KEY = CommonPlugin.PLUGIN_ID
			+ "." + "analysis_type";

	public static final String IPC_SOURCE_ACTIONS = CommonPlugin.PLUGIN_ID
			+ "." + "source_actions";

	public static final String IPC_TARGET_ACTIONS = CommonPlugin.PLUGIN_ID
			+ "." + "target_actions";

	public static final String IPC_START_TIME = CommonPlugin.PLUGIN_ID + "."
			+ "start_time";

	public static final String IPC_STOP_TIME = CommonPlugin.PLUGIN_ID + "."
			+ "stop_time";

	public static final String IPC_TIME_STEP = CommonPlugin.PLUGIN_ID + "."
			+ "time_step";

	public static final String IPC_ADVANCED_ARGUMENTS = CommonPlugin.PLUGIN_ID
			+ ".advanced_arguments";
	/**
	 * It can be either a variable or a full OS-dependent path
	 */
	public static final String IPC_PEPA_FILE_NAME = CommonPlugin.PLUGIN_ID
			+ "." + "filename";

	/**
	 * It can be either a variable or a full OS-dependent path
	 */
	public static final String IPC_OUTPUT_DIRECTORY = CommonPlugin.PLUGIN_ID
			+ "." + "output_directory";

	/**
	 * Whether to perform static analysis on the model
	 */
	public static final String IPC_STATIC_ANALYSIS = CommonPlugin.PLUGIN_ID
			+ "." + "static_analysis";

	/**
	 * Whether to use a location-aware probe
	 */
	public static final String IPC_USE_LOCATION_AWARE_PROBE = CommonPlugin.PLUGIN_ID
			+ "." + "location_aware";
	
	/**
	 * Probed component name
	 */
	public static final String IPC_PROBE_COMPONENT = CommonPlugin.PLUGIN_ID + "." + "attached_component";

	/**
	 * Standard filename for passage time results
	 */
	public static final String PASSAGE_TIME_RESULTS_FILE_NAME = "PT_RESULTS";

	/**
	 * Analysis types go here
	 */
	public static final String IPC_STEADY_STATE = "steady";

	public static final String IPC_PASSAGE_TIME = "passage";

	public static final String IPC_TRANSIENT_ANALYSIS = "transient";

	/**
	 * Solver names go here
	 */
	public static final String IPC_GAUSS = "gauss";

	public static final String IPC_GRASSMAN = "grassman";

	public static final String IPC_GAUSS_SEIDEL = "gauss_seidel";

	public static final String IPC_SOR = "sor";

	public static final String IPC_BICG = "bicg";

	public static final String IPC_CGNR = "cgnr";

	public static final String IPC_BICGSTAB = "bicgstab";

	public static final String IPC_BICGSTAB2 = "bicgstab2";

	public static final String IPC_CGS = "cgs";

	public static final String IPC_TFQMR = "tfqmr";

	public static final String IPC_AI = "ai";

	public static final String IPC_AIR = "air";

	public static final String IPC_AUTOMATIC = "automatic";

	public static final boolean DEFAULT_LOGGING = false;

	public static final boolean DEFAULT_USE_AGGREGATION = true;

	public static final String DEFAULT_SOLVER = IPC_SOR;

	public static final String DEFAULT_ANALYSIS = IPC_PASSAGE_TIME;

	public static final String DEFAULT_SOURCE = "";

	public static final String DEFAULT_TARGET = "";

	public static final String DEFAULT_START_TIME = "0.1";

	public static final String DEFAULT_STOP_TIME = "10.0";

	public static final String DEFAULT_TIME_STEP = "1.0";

	public static final String DEFAULT_STATIC_ANALYSIS = "false";
	
	/**
	 * By default, it uses a location-unaware probe
	 */
	public static final String DEFAULT_LOCATION_AWARE = "false";

}
