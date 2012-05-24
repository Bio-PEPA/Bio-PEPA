package uk.ac.ed.inf.biopepa.cl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import uk.ac.ed.inf.biopepa.core.BioPEPA;
import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.Utilities;
import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.compiler.ProblemInfo;
import uk.ac.ed.inf.biopepa.core.compiler.ProblemInfo.Severity;
import uk.ac.ed.inf.biopepa.core.dom.AddReactionTracer;
import uk.ac.ed.inf.biopepa.core.dom.ISourceRange;
import uk.ac.ed.inf.biopepa.core.dom.Model;
import uk.ac.ed.inf.biopepa.core.dom.OverrideValueVisitor;
import uk.ac.ed.inf.biopepa.core.dom.SimpleSourceRange;
import uk.ac.ed.inf.biopepa.core.dom.internal.ParserException;
import uk.ac.ed.inf.biopepa.core.imports.NetworKinImport;
import uk.ac.ed.inf.biopepa.core.imports.NetworKinLine;
import uk.ac.ed.inf.biopepa.core.imports.NetworKinTranslate;
import uk.ac.ed.inf.biopepa.core.interfaces.ProgressMonitor;
import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.interfaces.Solver;
import uk.ac.ed.inf.biopepa.core.sba.FileStringConsumer;
import uk.ac.ed.inf.biopepa.core.sba.InvariantInferer;
import uk.ac.ed.inf.biopepa.core.sba.MidiaOutput;
import uk.ac.ed.inf.biopepa.core.sba.ModuleExtractor;
import uk.ac.ed.inf.biopepa.core.sba.OutlineAnalyser;
import uk.ac.ed.inf.biopepa.core.sba.Parameters;
import uk.ac.ed.inf.biopepa.core.sba.Parameters.Parameter;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.SBAReaction;
import uk.ac.ed.inf.biopepa.core.sba.SimpleTree;
import uk.ac.ed.inf.biopepa.core.sba.Solvers;
import uk.ac.ed.inf.biopepa.core.sba.StandardOutStringConsumer;
import uk.ac.ed.inf.biopepa.core.sba.StringConsumer;
import uk.ac.ed.inf.biopepa.core.sba.VennInference;
import uk.ac.ed.inf.biopepa.core.sba.export.PrismExport;
import uk.ac.ed.inf.biopepa.core.sba.export.SBMLExport;
import uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer;



public class BioPEPACommandLine {	
	/*
	 * We use this for exceptions involving badly formatted command
	 * line options, for example if the --start-time argument is not
	 * followed by a double, or any argument at all.
	 */
	@SuppressWarnings("serial")
	public static class WrongArgumentsException extends Exception {

		public WrongArgumentsException(String message) {
			super(message);
		}
	}

	
	public static enum Operation { 
		NoOperation, Help, Version, TimeSeries, ImportSpread,
		Distribution, DoOutline, DoInvariants, DoInvariantsCheck,
		Modularise, CheckModel,
		ExtractModule, ExportPrism, ExportSBML, DoVenn,
	};

	private static boolean argumentEqualsOption (String arg, String option){
		return arg.equals("-" + option) || arg.equals("--" + option);
	}
	
	private static Operation getOperation (String[] args){
		if (args.length == 0){
			return Operation.Help;
		}
		String opString = args[0];
		// Strings are frustratingly absent from switch statements
		// in Java, so I'm afraid it's a huge if statement, not
		// my fault :(
		// Note that for help and version we allow it to be an option
		// like looking command, so we allow
		// biopepa --help to be a synonum for biopepa help
		if (opString.equals("help") || 
				argumentEqualsOption(opString, "help")){
			return Operation.Help;
		} else if (opString.equals("version") ||
				argumentEqualsOption(opString, "version")){
			return Operation.Version;
		} else if (opString.equals("timeseries")){
			return Operation.TimeSeries;
		} else if (opString.equals("distribution")){
			return Operation.Distribution;
		} else if (opString.equals("import-spread")){
			return Operation.ImportSpread;
		} else if (opString.equals("outline")){
			return Operation.DoOutline;
		} else if (opString.equals("invariants")){
			return Operation.DoInvariants;
		} else if (opString.equals("invariants-check")){
			return Operation.DoInvariantsCheck;
		} else if (opString.equals("modularise")){
			return Operation.Modularise;
		} else if (opString.equals("check")){
			return Operation.CheckModel;
		} else if (opString.equals("extract-module")){
			return Operation.ExtractModule;
		} else if (opString.equals("prism")){
			return Operation.ExportPrism;
		} else if (opString.equals("export-sbml")){
			return Operation.ExportSBML;
		} else if (opString.equals("venn")){
			return Operation.DoVenn;
		} else {
			return Operation.NoOperation;
		}
	}
	
	// Interprets the overrides given on the command line
	private static Map <String, String> obtainOverrides(String[] args) 
										throws WrongArgumentsException{
		HashMap<String, String> mapping = new HashMap<String, String> ();
		for (int index = 0; index < args.length; index++){
			String argument = args[index];
			if (argument.equals("-setValue") ||
					argument.equals("--setValue") ||
					argument.equals("--set-value")){
				if (args.length > index + 2){
					mapping.put(args[index+1], args[index+2]);
					index += 2;
				} else {
					String m = argument + " must take two arguments";
					throw new WrongArgumentsException (m);
				}
			}
		}
		
		return mapping;
	}
	
	/*
	 * Returns the argument to a given option, or null if this option
	 * does not occur. Note that we return the first such option argument
	 * so if there are multiple we do not return them.
	 */
	private static String getOptionArgument(String[] args, String option) 
								throws WrongArgumentsException{
		for (int index = 0; index < args.length; index++){
			if (argumentEqualsOption(args[index], option)){
				if (args.length > index + 1){
					return args[index + 1];
				} else {
					String m = "The " + args[index] + " option must take an" +
					" argument";
					throw new WrongArgumentsException(m);
				}
			}
		}
		// If the option did not occur then simply return null
		return null;
	}
	
	/*
	 * Returns a list of arguments to the given option name. This is essentially
	 * the same as 'getOptionArgument' except it is used when you expect there to
	 * be more than one option of the given name. 
	 * For example: --module A,B --module C,D
	 */
	private static List<String> getAllOptionArguments(String[] args, String option)
	        throws WrongArgumentsException {
		LinkedList<String> result = new LinkedList<String>();
		
		for (int index = 0; index < args.length; index++){
			if (argumentEqualsOption(args[index], option)){
				if (args.length > index + 1){
					index++;
					result.addLast(args[index]);
				} else {
					String m = "The " + args[index] + " option must take an argument";
					throw new WrongArgumentsException(m);
				}
			}
		}
		
		return result;
	}
	
	private static List<ReactionTracer> getReactionTracers (String[] args) 
			throws WrongArgumentsException{
		List<ReactionTracer> rTracers= new LinkedList<ReactionTracer> ();
		for (int index = 0; index < args.length; index++){
			String argument = args[index];
			if (argument.equals("-reaction-tracer") ||
					argument.equals("--reaction-tracer") ||
					argument.equals("--reactionTracer")){
				if (args.length > index + 2){
					rTracers.add(new ReactionTracer(args[index + 1],
													args[index + 2]));
					index += 2;
				} else {
					String m = argument + " must take two arguments";
					throw new WrongArgumentsException (m);
				}
			}
		}
		
		return rTracers;
	}
	
	private static Solver getSolverArgument(String[] args) 
							throws WrongArgumentsException{
		Solver solver = null;
		String solverName = getOptionArgument(args, "solver");
		if (solverName == null){
			solverName = "gillespie"; // the default;
		}
		
		solver = Solvers.getSolverInstance(solverName);
		if (solver == null){
			String m = "Do not recognise the solver name: " + solverName;
			throw new WrongArgumentsException (m);
		}
		return solver;
	}
	
	private static double getDoubleOptionArgument(String[] args, 
			String option, double defaultValue) throws WrongArgumentsException{
		String doubleString = getOptionArgument(args, option);
		double value = defaultValue;
		if (doubleString != null){
			try {
				value = Double.parseDouble(doubleString);
			} catch (NumberFormatException e){
				String m = "The " + option + 
							" argument incorrectly formatted: " + 
							doubleString;
				throw new WrongArgumentsException(m);
			}
		}
		
		return value;
	}
	
	private static int getIntegerOptionArgument(String[] args, 
			String option, int defaultValue) throws WrongArgumentsException{
		String intString = getOptionArgument(args, option);
		int value = defaultValue;
		if (intString != null){
			try {
				value = Integer.parseInt(intString);
			} catch (NumberFormatException e){
				String m = "The " + option + 
							" argument incorrectly formatted: " + 
							intString;
				throw new WrongArgumentsException(m);
			}
		}
		
		return value;
	}
	
	/*
	 * The same as the above option to get an integer option argument
	 * however we also allow characters to be used as replacements for
	 * numbers. So for example 
	 * --option a
	 * is the same as --option 0
	 * and 
	 * --option d
	 * is the same as --option 4
	 * Similarly A=0 and D=4.
	 */
	private static int getCharOrIntOptionArgument(String[] args, 
			String option, int defaultValue) throws WrongArgumentsException{
		
		String intString = getOptionArgument(args, option);
		
		// If there is no such option then we can immediately just
		// return the default value.
		if (intString == null){
			return defaultValue;
		}
		
		/*
		 * Basically then if the string is of length one and is character
		 * between 'a' and 'z' or 'A' and 'Z' then we can return the
		 * corresponding integer. Otherwise just fall-through and do the
		 * usual parsing of a number (even if the string is of length one,
		 * for example if it is '1'.
		 */
		if (intString.length() == 1){
			char ch = intString.charAt(0);
			
			if (ch >= 'a' && ch <= 'z'){
				return ch - 'a';
			}
			if (ch >= 'A' && ch <= 'Z'){
				return ch - 'A';
			}
		}
		
		int value = defaultValue;
		try {
			value = Integer.parseInt(intString);
		} catch (NumberFormatException e){
			String m = "The " + option + 
						" argument incorrectly formatted: " + 
						intString;
			throw new WrongArgumentsException(m);
		}
				
		return value;
	}
	
	// Returns all the (single) arguments of a given flag. 
	private static List<String> getAllStringArguments(String flag, 
			                                          String[] args){
		LinkedList<String> results = new LinkedList<String>();
		for (int index = 0; index < args.length; index++){
			if (argumentEqualsOption(args[index], flag) &&
			    (index + 1) < args.length){
				index++;
				results.addLast(args[index]);
			}
		}
		return results;
	}
	
	
	private static int getIndepentRuns(String[] args, int defaultValue) 
							throws WrongArgumentsException{
		return getIntegerOptionArgument(args, "runs", defaultValue);
	}
	private static int getNumberDataPoints(String[] args)
							throws WrongArgumentsException{
		return getIntegerOptionArgument(args, "dataPoints", 100);
	}
	private static double getDataPointSize(String[] args)
			throws WrongArgumentsException {
		return getDoubleOptionArgument(args, "point-size", 0.1);
	}
	
	private static double getStartTime (String[] args) 
							throws WrongArgumentsException{
		return getDoubleOptionArgument(args, "startTime", 0.0);
	}
	
	private static double getStopTime (String [] args) 
							throws WrongArgumentsException{
		return getDoubleOptionArgument(args, "stopTime", 10.0);
	}
	
	private static double getTimeStep (String [] args) 
							throws WrongArgumentsException{
		return getDoubleOptionArgument(args, "timeStep", 0.0001);
	}
	
	private static double getRelativeError(String [] args)
							throws WrongArgumentsException{
		double defaultValue = (Double) Parameter.Relative_Error.getDefault();
		return getDoubleOptionArgument(args, "relative-error", defaultValue);
	}
	
	private static double getAbsoluteError(String [] args)
							throws WrongArgumentsException{
		double defaultValue = (Double) Parameter.Absolute_Error.getDefault();
		return getDoubleOptionArgument(args, "absolute-error", defaultValue);
	}
	
	/*
	 * Returns true if the given flag is present in the command-line
	 * generally useful for implementing boolean flags such as --verbose.
	 */
	private static boolean commandHasFlag (String flag, String[] args){
		for (String arg : args){
			if (argumentEqualsOption(arg, flag)){
				return true;
			}
		}
		return false;
	}
		
	/*
	 * We might be able to do a char argument?
	 * This would be quite nice.
	 */
	private static int getProteinColumn(String[] args) 
		throws WrongArgumentsException{
		return getCharOrIntOptionArgument(args, "protein-column", 1);
	}
	private static int getKinaseColumn(String[] args) 
		throws WrongArgumentsException{
		return getCharOrIntOptionArgument(args, "kinase-column", 0);
	}
	private static int getResidueColumn(String[] args) 
		throws WrongArgumentsException{
		return getCharOrIntOptionArgument(args, "residue-column", 2);
	}
	
	private static String getOutputFile (String[] args) 
						throws WrongArgumentsException{
		return getOptionArgument(args, "output-file");
	}
	
	private static String getTargetIdentifier (String [] args) 
			throws WrongArgumentsException{
		return getOptionArgument(args, "target-comp");
	}
	
	private static int getTargetValue (String [] args)
			throws WrongArgumentsException {
		return getIntegerOptionArgument (args, "target-value", 0);
	}
	
	private static int getLevelSize (String [] args)
	        throws WrongArgumentsException {
		return getIntegerOptionArgument(args, "level-size", 1);
	}
	
	private static int getGranularity (String [] args)
			throws WrongArgumentsException {
		return getIntegerOptionArgument(args, "granularity", 1);
	}
	
	private static String[] splitCommaSeparatedNames(String compsString){
		// I copied this from:
		// http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
		// We could actually think about what BioPEPA names can be and how
		// this affects it.
		String[] tokens = compsString.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		return tokens;
	}
	
	private static String[] getComponentsArgument(String[] args)
			throws WrongArgumentsException{
		String compsString = getOptionArgument(args, "components");
		if (compsString == null){
			return null;
		}

		return splitCommaSeparatedNames(compsString);
	}
	
	private static List<String[]> getModuleGroups(String[] args)
	        throws WrongArgumentsException {
		List<String> module_strings = getAllOptionArguments(args, "module");
		LinkedList<String[]> result = new LinkedList<String[]>();
		
		for (String module_string : module_strings){
			result.addLast(splitCommaSeparatedNames(module_string));
		}
		
		return result;
	}
	
	private static String[] getKnockedOutReactions(String[] args)
			throws WrongArgumentsException{
		String reactionsString = getOptionArgument(args, "knock-out");
		if (reactionsString == null){
			return null;
		}

		// I copied this from:
		// http://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
		// We could actually think about what BioPEPA names can be and how
		// this affects it.
		String[] tokens = 
			reactionsString.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		return tokens;
	}
	
	private static String[] commandHelpString = {
		"The general form should be: biopepa COMMAND [OPTIONS + ARGUMENTS]",
		// Remember to add new commands here
		"The command can be any of: help, version, timeseries, import-spread",
		"biopepa help                   -- prints out this help message",
		"biopepa version                -- prints out the version message",
		"biopepa timeseries FILENAME    -- performs a time series analysis",
		"biopepa outline FILENAME       -- output the outline of the model",
		"biopepa invariants FILENAME    -- compute the invariants of the model",
		"biopepa invariants-check FILE  -- compute invariants and check they",
		"                               -- sane by ignore sinks and sources",
		"biopepa check FILENAME         -- perform static analyses over " + 
		                                   "the model",
		"biopepa distribution FILENAME  -- calculate a CDF",
		"biopepa import-spread FILENAME -- transform a spreadsheet into a " +
											"biopepa model",
	    "biopepa prism FILENAME         -- export the model as a prism model",
	    "biopepa extract-module FILE    -- extract a module from a model",
	    "biopepa export-sbml FILENAME   -- export the model as an SBML model",
	    "biopepa modularise FILENAME    -- export a MIDIA Rscript based on " +
	                                      "the input biopepa model",
	 // "biopepa export-module FILENAME -- extract the given components as " +
	 //                                    "a submodel",

		// And also had a oneline description of a new command here (as above)
		"",
		"More help on any of the individual biopepa commands can",
		"be obtained by providing the \"--help\" option to any command",
		"for example:",
		"biopepa timeseries -help"
	};
	
	private static String[] versionString = {
		"This is biopepa version 0.1"
	};
	
	
	private static String[] getTimeSeriesHelpString (){
		// For the solver option
		String[] shortNameArray = Solvers.getSolverShortNameList();
		String nameList = shortNameArray[0];
		for (int index = 1; index < shortNameArray.length; index++){
			nameList = nameList + ", " + shortNameArray[index];
		}
		String[] result = { 
		"The following flags and options are permitted to follow a",
		"timeseries biopepa command",
		"biopepa timeseries FILENAME ... ",
		"   --help        -- prints out this message",
		"   --show-time   -- prints out the time taken for the analysis",
		"   --verbose     -- prints out what is happening",
		"   --runs        -- set the number of independent simulation",
		"                    replications",
		"   --solver      -- set the solver to be used; this may be one of the",
		"                    following: ",
	    "                    " + nameList,
		"   --startTime   -- set the start time of the results",
		"   --stopTime    -- set the stop time of the simulation and results",
		"   --dataPoints  -- set the number of data points to plot",
		"                    in the results",
		// Todo, translate this into a data-points Parameter
		// "   --point-size  -- set the distant between time points in the results",
		"   --timeStep    -- set the time step of the simulation, used for",
		"                    ODE simulation only",
		"   --absolute-error -- set the absolute error for an ODE solver",
		"   --relative-error -- set the relative error for an ODE solver",
		"   --output-file -- set the file to write the output to",
		"   --reaction-tracer [REACTION] [COMPONENT NAME]",
		"                 -- Adds a reaction tracer component which can be",
		"                    used to track how many firings of the given",
		"                    reaction have occurred",
		"   --no-warnings    -- do not print warning messages",
		"   --set-value      -- takes two arguments, a name and value.",
		"                    -- the given name will be overridden with the",
		"                    -- given value, it may be an initial population",
		"                    -- or a rate constant.",

		};
		
		return result;
	}
	
	private static String[] distributionHelpString = {
			"The following flags and options are permitted to follow a",
			"distribution biopepa command",
			"biopepa import-spread FILENAME ...",
			"   --help           -- prints out this message",
			"   --verbose     -- prints out what is happening",
			"   --output-file    -- set the file to write the output to",
			"   --target-comp    -- set the component to check for target" +
									" completion",
			"   --target-value   -- set the target value for completion. ",
		    "                       both 'target-' arguments are non-optional.",
		    "                       We compute the probability at time t, that",
		    "                       from the initial state a state was reached",
		    "                       in which the target component (or variable)",
		    "                       has a population equal to or higher than",
		    "                       the given target value.",
		    "   --startTime   -- set the start time of the results",
			"   --stopTime    -- set the stop time of the simulation",
		    "                    and results",
			"   --point-size  -- set the distant between time points in",
		    "                    the results",
			"   --runs        -- set the number of independent simulation",
			"                    replications",
			"   --reaction-tracer [REACTION] [COMPONENT NAME]",
			"                 -- Adds a reaction tracer component which can be",
			"                    used to track how many firings of the given",
			"                    reaction have occurred. Often used as:",
			"                    --reaction-tracer r E --target-comp E ...",
			"   --no-warnings    -- do not print warning messages",
			"   --set-value      -- takes two arguments, a name and value.",
			"                    -- the given name will be overridden with the",
			"                    -- given value, it may be an initial population",
			"                    -- or a rate constant.",

	};
	
	private static String[] importSpreadHelpString = {
		"The following flags and options are permitted to follow a",
		"import-spread biopepa command",
		"biopepa import-spread FILENAME ...",
		"   --help           -- prints out this message",
		"   --output-file    -- set the file to write the output to",
		"   --protein-column -- sets the column from which to take the protein",
		"                       The columns are index from 0 and can also be",
		"                       given as letters as in a spreadsheet",
		"                       eg, --protein-column C",
		"   --kinase-column  -- As --protein-column but for the kinase column",
		"   --residue-column -- As --protein-column but for the residue column",
		"   --show-narrative -- Print out the narrative description of the",
		"                       imported spreadsheet, eg",
		"                       Kinase1 phosphorylates Protein1 on residue R1",
		"   --show-reactions -- Prints out the inferred set of reactions as",
		"                       well as the translated bioPEPA components",


	};
	
	private static String[] doOutlineHelpString = {
		"The following flags and options are permitted to follow an",
		"outline biopepa command",
		"biopepa outline FILENAME ...",
		"   --help           -- prints out this message",
		"   --output-file    -- set the file to write the output to",
		"   --no-warnings    -- do not print warning messages",
		"   --set-value      -- takes two arguments, a name and value.",
		"                    -- the given name will be overridden with the",
		"                    -- given value, it may be an initial population",
		"                    -- or a rate constant.",

	};
	
	private static String[] checkModelHelpString = {
		"The following flags and options are permitted to follow a",
		"check biopepa command",
		"biopepa check FILENAME ...",
		"   --help           -- prints out this message",
		"   --output-file    -- set the file to write the output to",
		"   --no-warnings    -- do not print warning messages (only errors)",
		"   --set-value      -- takes two arguments, a name and value.",
		"                    -- the given name will be overridden with the",
		"                    -- given value, it may be an initial population",
		"                    -- or a rate constant.",

	};
	
	private static String[] doInvariantsHelpString = {
		"The following flags and options are permitted to follow an",
		"invariants biopepa command",
		"biopepa invariants FILENAME ...",
		"   --help           -- prints out this message",
		"   --output-file    -- set the file to write the output to",
		"   --no-warnings    -- do not print warning messages",
		"   --knock-out      -- comma separated list of reaction names",
		"                       which should be ignored for the purposes",
		"                       of this analysis.",
		"   --set-value      -- takes two arguments, a name and value.",
		"                    -- the given name will be overridden with the",
		"                    -- given value, it may be an initial population",
		"                    -- or a rate constant.",
		"   --ignore-sinks   -- ignore sink reactions when computing invariants",
		"   --ignore-sources -- ignore source reactions when computings invariants",

	};
	
	private static String[] doInvariantsCheckHelpString = {
		"The following flags and options are permitted to follow an",
		"invariants-check biopepa command",
		"biopepa invariants-check FILENAME ...",
		"   --help           -- prints out this message",
		"   --output-file    -- set the file to write the output to",
		"   --no-warnings    -- do not print warning messages",
		"   --knock-out      -- comma separated list of reaction names",
		"                       which should be ignored for the purposes",
		"                       of this analysis.",
		"   --set-value      -- takes two arguments, a name and value.",
		"                    -- the given name will be overridden with the",
		"                    -- given value, it may be an initial population",
		"                    -- or a rate constant.",

	};
	
	private static String[] doVennHelpString = {
		"The following flags and options are permitted to follow an",
		"venn biopepa command",
		"biopepa venn FILENAME ...",
		"   --help           -- prints out this message",
		"   --output-file    -- set the file to write the output to",
		"   --no-warnings    -- do not print warning messages",
	};
	
	private static String[] doModulariseHelpString = {
		"The following flags and options are permitted to follow an",
		"modularise biopepa command",
		"biopepa modularise FILENAME ...",
		"   --help           -- prints out this message",
		"   --output-file    -- set the file to write the output to",
		"   --no-warnings    -- do not print warning messages",
		"   --transpose      -- compute reaction modules instead of component",
		"   --granularity    -- the smallest size of module",
		"   --set-value      -- takes two arguments, a name and value.",
		"                    -- the given name will be overridden with the",
		"                    -- given value, it may be an initial population",
		"                    -- or a rate constant.",

	};
	
	private static String[] extractModuleHelpString = {
		"The following flags and options are permitted to follow an",
		"extract-module biopepa command",
		"biopepa extract-module FILENAME ...",
		"   --help           -- prints out this message",
		"   --output-file    -- set the file to write the output to",
		"   --no-warnings    -- do not print warning messages",
		"   --components     -- comma separated list of components to",
		"                       extract as a sub-model",
		"   --set-value      -- takes two arguments, a name and value.",
		"                    -- the given name will be overridden with the",
		"                    -- given value, it may be an initial population",
		"                    -- or a rate constant.",

	};
	
	private static String[] exportPrismHelpString = {
		"The following flags and options are permitted to follow an",
		"prism biopepa command",
		"biopepa prism FILENAME ...",
		"   --help           -- prints out this message",
		"   --output-file    -- set the file to write the output to",
		"   --no-warnings    -- do not print warning messages",
		"   --level-size     -- sets the level size for the prism output.",
		"",
		"The output files should be set with:", 
		"--output-file name.pm --output-file name.csl",
		"one must have the \".pm\" extension and one must have the \".csl\"",
		"extension, if either is not present then its associated contents",
		"will be output to the console.",
		"",
		"   --set-value      -- takes two arguments, a name and value.",
		"                    -- the given name will be overridden with the",
		"                    -- given value, it may be an initial population",
		"                    -- or a rate constant.",
	};
	
	private static String[] exportSBMLHelpString = {
		"The following flags and options are permitted to follow an",
		"export sbml biopepa command",
		"biopepa export-sbml FILENAME ...",
		"   --help           -- prints out this message",
		"   --output-file    -- set the file to write the output to",
		"   --no-warnings    -- do not print warning messages",
		"",
		"   --set-value      -- takes two arguments, a name and value.",
		"                    -- the given name will be overridden with the",
		"                    -- given value, it may be an initial population",
		"                    -- or a rate constant.",
	};
	
	private static void printHelpString (String[] helpString){
		for (String line : helpString){
			printUserInformation(line);
		}
	}
	
	// private static void checkValidityAndGetNonFlagArgs
	/*
	 * If we wish to have the file argument[s] anywhere in the
	 * command-line then we really want a more general method of
	 * command-line argument/flag specfication, something like:
	 * class Flag {
	 *    public String name;
	 *    public Char short;
	 *    public int noOfArgs
	 * }
	 * and then:
	 * doArguments
	 * for (int i ...){
	 *    if exists Flag such that args[i].equals(flag.name) (or short){
	 *       i += flag.noOfArgs
	 *    } else {
	 *       it's a non-flag argument or if it starts with -- then it's
	 *       an unrecognised argument.
	 *    } 
	 * }
	 *   
	 */
	/* But for now we'll just assume that there is only one filename
	 * and that it may be 'stdin' for the standard in.
	 */
	
	/**
	 * @param args
	 */
	public static void main(String[] commandLineArgs) throws BioPEPAException {
	    Operation operation = getOperation (commandLineArgs);
	    String[] operationArgs;
	    if (operation.equals(Operation.NoOperation)){
	    	// The default is time series
	    	operation = Operation.TimeSeries;
	    	operationArgs = commandLineArgs;
	    } else {
	    	// If there was a commmand we take that out of the
	    	// list of options passed into the options passed to
	    	// the individual command. A bit clumsy to copy almost the whole
	    	// array but we aren't expecting more than a handful of arguments.
	    	operationArgs = new String [commandLineArgs.length - 1];
	    	for (int index = 0; index < operationArgs.length; index++){
	    		operationArgs[index] = commandLineArgs[index + 1];
	    	}
	    }
	    /*
	     * Now we know what the operation is and can find out what
	     * the arguments are and what the flags are, and also which
	     * flags are relevant etc.
	     */
	    
	    if (operation.equals(Operation.Help)){
	    	printHelpString(commandHelpString);
	    	System.exit(0);
	    }
	    
	    if (operation.equals(Operation.Version)){
	    	printHelpString (versionString);
	    	System.exit(0);
	    }
	    
	    if (operation.equals(Operation.TimeSeries)){
	    	if (commandHasFlag("help", operationArgs)){
	    		printHelpString (getTimeSeriesHelpString());
	    	} else {
	    		try {
	    			performTimeSeries(operationArgs);
	    		} catch (WrongArgumentsException e){
	    			printUserError(e.getMessage());
	    			System.exit(1);
	    		}
	    	}
	    }
	    
	    if (operation.equals(Operation.Distribution)){
	    	if (commandHasFlag ("help", operationArgs)){
	    		printHelpString (distributionHelpString);
	    	} else {
	    		try {
	    			performDistribution (operationArgs);
	    		} catch (WrongArgumentsException e){
	    			printUserError(e.getMessage());
	    			System.exit(1);
	    		}
	    	}
	    }
	    
	    if (operation.equals(Operation.DoOutline)){
	    	if (commandHasFlag ("help", operationArgs)){
	    		printHelpString (doOutlineHelpString);
	    	} else {
	    		try {
	    			performDoOutline (operationArgs);
	    		} catch (WrongArgumentsException e){
	    			printUserError(e.getMessage());
	    			System.exit(1);
	    		}
	    	}
	    }  
	    
	    if (operation.equals(Operation.CheckModel)){
	    	if (commandHasFlag ("help", operationArgs)){
	    		printHelpString (checkModelHelpString);
	    	} else {
	    		try {
	    			performCheckModel (operationArgs);
	    		} catch (WrongArgumentsException e){
	    			printUserError(e.getMessage());
	    			System.exit(1);
	    		}
	    	}
	    }  
	    
	    if (operation.equals(Operation.Modularise)){
	    	if (commandHasFlag ("help", operationArgs)){
	    		printHelpString (doModulariseHelpString);
	    	} else {
	    		try {
	    			performModularise (operationArgs);
	    		} catch (WrongArgumentsException e){
	    			printUserError(e.getMessage());
	    			System.exit(1);
	    		}
	    	}
	    } 
	    
	    if (operation.equals(Operation.ExtractModule)){
	    	if (commandHasFlag ("help", operationArgs)){
	    		printHelpString(extractModuleHelpString);
	    	} else {
	    		try {
	    			performExtractModule (operationArgs);
	    		} catch (WrongArgumentsException e) {
	    			printUserError(e.getMessage());
	    			System.exit(1);
	    		}
	    	}
	    }
	    
	    if (operation.equals(Operation.ExportPrism)){
	    	if (commandHasFlag("help", operationArgs)){
	    		printHelpString(exportPrismHelpString);
	    	} else {
	    		try {
	    			performExportPrism (operationArgs);
	    		} catch (WrongArgumentsException e){
	    			printUserError(e.getMessage());
	    			System.exit(1);
	    		}
	    	}
	    }
	    
	    if (operation.equals(Operation.ExportSBML)){
	    	if (commandHasFlag("help", operationArgs)){
	    		printHelpString (exportSBMLHelpString);
	    	} else {
	    		try {
	    			performExportSBML (operationArgs);
	    		} catch (WrongArgumentsException e){
	    			printUserError(e.getMessage());
	    			System.exit(1);
	    		}
	    	}
	    }
	    
	    if (operation.equals(Operation.DoInvariants)){
	    	if (commandHasFlag ("help", operationArgs)){
	    		printHelpString (doInvariantsHelpString);
	    	} else {
	    		try {
	    			performDoInvariants (operationArgs);
	    		} catch (WrongArgumentsException e){
	    			printUserError(e.getMessage());
	    			System.exit(1);
	    		}
	    	}
	    }
	    
	    if (operation.equals(Operation.DoInvariantsCheck)){
	    	if (commandHasFlag("help", operationArgs)){
	    		printHelpString(doInvariantsCheckHelpString);
	    	} else {
	    		try {
	    			performDoInvariantsCheck (operationArgs);
	    		} catch (WrongArgumentsException e){
	    			printUserError(e.getMessage());
	    			System.exit(1);
	    		}
	    	}
	    }
	    
	    if (operation.equals(Operation.DoVenn)){
	    	if (commandHasFlag("help", operationArgs)){
	    		printHelpString(doVennHelpString);
	    	} else {
	    		try {
	    			performDoVenn (operationArgs);
	    		} catch (WrongArgumentsException e){
	    			printUserError(e.getMessage());
	    			System.exit(1);
	    		}
	    	}
	    }
	    
	    if (operation.equals(Operation.ImportSpread)){
	    	if (commandHasFlag("help", operationArgs)){
	    		printHelpString (importSpreadHelpString);
	    	} else {
	    		try {
	    			performImportSpread(operationArgs);
	    		} catch (WrongArgumentsException e) {
	    			printUserError(e.getMessage());
					e.printStackTrace();
					System.exit(1);
				}
	    	}
	    }
	}   
	
	/*
	private static Writer getOutWriter (String[] args)
					throws WrongArgumentsException, IOException{
		String outputFile = getOutputFile(args);
		Writer outWriter;
		if (outputFile == null){
			OutputStreamWriter osw = new OutputStreamWriter(System.out);
			outWriter = new BufferedWriter(osw);
		} else {
			FileWriter fw = new FileWriter (outputFile);
			outWriter = new PrintWriter(new BufferedWriter(fw));
		}
		
		return outWriter;
	}
	*/
	
	private static StringConsumer fileNameToStringConsumer(String outputFile)
					throws IOException{
		if (outputFile == null){
			StandardOutStringConsumer sosc = new StandardOutStringConsumer();
			sosc.openStringConsumer();
			return sosc;
		} else {
			FileStringConsumer fsc = new FileStringConsumer(outputFile);
			fsc.openStringConsumer();
			return fsc;
		}
	}
	
	private static StringConsumer getOutStringConsumer(String[] args)
	                 throws WrongArgumentsException, IOException{
		String outputFile = getOutputFile(args);
		return fileNameToStringConsumer(outputFile);
	}
	
	/* The same as the above except that the output file in question
	 * must have the given extension.
	 */
	private static StringConsumer getOutExtStringConsumer(String ext, 
														  String[] args)
	                  throws WrongArgumentsException, IOException{
		List<String> possibles = getAllStringArguments("output-file", args);
		String outputFile = null;
		
		for (String possible : possibles){
			if (hasExtension(ext, possible)){
				outputFile = possible;
				break;
			}
		}
		
		return fileNameToStringConsumer(outputFile);
	}
	
	private static boolean hasExtension (String ext, String filename){
		return filename.endsWith("." + ext);
	}
	
	private static void performImportSpread (String[] args) 
			throws WrongArgumentsException{
		String filename = args[0];
		try{
			FileReader reader = new FileReader(filename);
			NetworKinImport nimport = new NetworKinImport();
			int kinase = getKinaseColumn(args);
			int protein = getProteinColumn(args);
			int residue = getResidueColumn(args);
			nimport.importWithReader(reader, kinase, protein, residue);
			List<NetworKinLine> nlines = nimport.getNetworKinLines();

			StringConsumer outWriter = getOutStringConsumer(args);
			
			if (commandHasFlag ("show-narrative", args)){
				for (NetworKinLine line : nlines){
					outWriter.appendLine(line.toNarrative());
				}
				outWriter.endLine();
			}
			
			
			
			NetworKinTranslate ntranslate = new NetworKinTranslate(nlines);
			ntranslate.translate();
			
			if (commandHasFlag("show-reactions", args)){
				outWriter.appendLine (ntranslate.reactionsString());
				outWriter.endLine();
			}
			
			outWriter.append (ntranslate.getBioPepaString());
			outWriter.closeStringConsumer();
			
		} catch (IOException e){
			printUserError ("There was a problem reading the file: " +
					filename);
			e.printStackTrace();
			System.exit(1);
		}
	}
		
	
	private static void performDoOutline (String [] args)
	        throws WrongArgumentsException
	{
		SBAModel sbaModel = parseAndComputeSBAModel(args);
		OutlineAnalyser outanalyser = new OutlineAnalyser();
		SimpleTree[] treearray = outanalyser.createOutlineTree(sbaModel);
		
		try {
			StringConsumer outWriter = getOutStringConsumer(args);
			for (SimpleTree tree : treearray){
				outWriter.append(tree.printTree());
			}
			outWriter.endLine();
			outWriter.closeStringConsumer();
		} catch (IOException e){
			printUserError ("There was a problem writing the output data");
			printUserError(e.getMessage());
		}
		
	}
	
	private static void performCheckModel (String [] args)
	        throws WrongArgumentsException
	{
		// We may worry about printing stuff out to the output
		// file etc, but for now this is good enough.
		/*SBAModel sbaModel =*/ parseAndComputeSBAModel(args);
	}
	
	private static void performModularise(String [] args)
    	throws WrongArgumentsException
    {
		SBAModel sbaModel = parseAndComputeSBAModel(args);
        MidiaOutput moutput = new MidiaOutput();
		if (commandHasFlag("transpose", args)){
			moutput.setTransposed(true);
		}
		moutput.setGranularity(getGranularity(args));
		try {
			StringConsumer outWriter = getOutStringConsumer(args);
			String output = moutput.produceMidiaOutput(sbaModel);
			outWriter.append(output);
			outWriter.endLine();
			outWriter.closeStringConsumer();
		} catch (IOException e){
			printUserError ("There was a problem writing the output data");
			printUserError(e.getMessage());
		}
    }
	
	private static void performExportPrism(String [] args)
    	throws WrongArgumentsException
    {
		ModelCompiler compiledModel = parseAndComputeModelCompiler (args);
		SBAModel sbaModel = computeSBAModel(compiledModel, args);
		
		PrismExport pexport = new PrismExport ();
		int levelSize = getLevelSize(args);
		pexport.setLevelSize(levelSize);
		pexport.setModel(compiledModel);
		pexport.setModel(sbaModel);
		try {
			StringConsumer pmOutWriter = getOutExtStringConsumer("pm", args);
			StringConsumer cslOutWriter = getOutExtStringConsumer("csl", args);
			// Obviously we should actually have two different writers
			// for the two different output files.
			pexport.export(pmOutWriter, cslOutWriter);
			pmOutWriter.endLine();
			pmOutWriter.closeStringConsumer();
			cslOutWriter.endLine();
			cslOutWriter.closeStringConsumer();
		} catch (IOException e){
			printUserError ("There was a problem writing the output data");
			printUserError(e.getMessage());
		}
    }
	
	private static void performExportSBML (String[] args)
	    throws WrongArgumentsException 
	{
		SBAModel sbaModel = parseAndComputeSBAModel(args);
		SBMLExport sbmlExport = new SBMLExport();
		sbmlExport.setModel(sbaModel);
		
		try {
			StringConsumer xmlOutWriter = getOutStringConsumer(args);
			xmlOutWriter.append(sbmlExport.toString());
			xmlOutWriter.closeStringConsumer();
		} catch (IOException e){
			printUserError ("There was a problem writing the output data");
			printUserError(e.getMessage());
		}
		
	}
	
	
	
	private static void performExtractModule(String [] args)
    	throws WrongArgumentsException
    {
		List<String[]> moduleGroups = getModuleGroups(args);
		
		if (moduleGroups.isEmpty()){
			printUserError ("You must supply at least one (comma separated) list of " +
					        "component names using the --module flag");
			
		}
		
		SBAModel sbaModel = parseAndComputeSBAModel(args);
		String outputFileBaseName = getOutputFile(args);

		// ComponentNode[] modelComponents = sbaModel.getComponents();
		System.out.println ("About to go into the for loop");
		for (int index = 0; index < moduleGroups.size(); index++){
			System.out.println ("I'm here, I'm here!");
			String[] componentNames = moduleGroups.get(index);

			// A mapping from component names to their initial concentrations
			// the idea here is that we could override what is written in the
			// model file with a command-line option.
			HashMap<String, Number> compMap = new HashMap<String, Number>();
			for (String component : componentNames) {
				// This currently does not raise an exception if the component
				// in question is not in the model, but we should catch that
				// situation and halt and print an error.
				Number count = sbaModel.getNamedComponentCount(component);
				compMap.put(component, count);
			}

			ModuleExtractor mextract = new ModuleExtractor(sbaModel, compMap);

			try {
				String file_suffix = "module_" + index + ".biopepa";
				String outputFile = outputFileBaseName != null ? 
						            outputFileBaseName + "_" + file_suffix : file_suffix ;
					                                               
				StringConsumer outWriter = fileNameToStringConsumer(outputFile);

				mextract.extract(outWriter);

				outWriter.endLine();
				outWriter.closeStringConsumer();
			} catch (IOException e) {
				printUserError("There was a problem writing the output data");
				printUserError(e.getMessage());
			} catch (BioPEPAException e) {
				printUserError(e.getMessage());
			}
		}
    }
	
	/*
	 * 
	Does not yet work, this uses MidiaInferer which is essentially
	to be a port of the MIDIA algorithm (written in R) to Java
	such that we do not need to make an external call to R.
	private static void performModularise(String [] args)
	        throws WrongArgumentsException
	{
		SBAModel sbaModel = parseAndComputeSBAModel(args);
		MidiaInferer minferer = new MidiaInferer();
		SimpleTree[] treearray = minferer.createMidiaTree(sbaModel);
		
		try {
			StringConsumer outWriter = getOutStringConsumer(args);
			for (SimpleTree tree : treearray){
				outWriter.append(tree.printTree());
			}
			outWriter.endLine();
			outWriter.closeStringConsumer();
		} catch (IOException e){
			printUserError ("There was a problem writing the output data");
			printUserError(e.getMessage());
		}
		
	}
	*/
	
	private static LinkedList<SBAReaction> getUsedReactionList(SBAModel sbaModel,
			boolean ignore_sources, boolean ignore_sinks,
			String[] knockedOutReactions){
		LinkedList<SBAReaction> usedReactions = new LinkedList<SBAReaction>();
		
		/*
		 * So basically we go through the list of all reactions in the model
		 * and add each reaction to the list of used reactions if it is not
		 * to be knocked out, either because it is explicitly knocked out
		 * or because it is a source and we are ignoring source reactions or
		 * it is a sink and we are ignoring sink reactions.
		 *
		 * A note here for our rather simple check if a reaction is a
		 * source or sink. We don't consider reactions such as:
		 * A -> A + B, as a source, even though it certainly introduces mass.
		 * The point is here that that reaction is likely wrong, and this
		 * analysis is designed to catch specifically those kinds of errors.
		 * Therefore if we simply ignore this kind of reaction we will 
		 * not catch as many errors as we might. Of course we may get something
		 * like a catalysed degredation
		 */
		for (SBAReaction reaction : sbaModel.getReactions()){
			int reactants = reaction.getReactants().size();
			int products = reaction.getProducts().size();
			String rName = reaction.getName();
			if ((reactants > 0 || !ignore_sources) &&
				(products > 0 || !ignore_sinks) &&
				!Utilities.arrayContains(knockedOutReactions, rName)){
				usedReactions.addLast(reaction);
			}
		}
		
		return usedReactions;
	}
	
	private static void performDoInvariants (String [] args)
	        throws WrongArgumentsException
	{	
		SBAModel sbaModel = parseAndComputeSBAModel(args);
		// These should be set by command-line flags
		boolean doStateInvariants = true;
		boolean doActivityInvariants = true;
		
		boolean ignore_sources = commandHasFlag("ignore-sources", args);
		boolean ignore_sinks = commandHasFlag("ignore-sinks", args);
		String[] knockedOutReactions = getKnockedOutReactions(args);
		
		LinkedList<SBAReaction> usedReactions =
			getUsedReactionList(sbaModel,
					            ignore_sources,
					            ignore_sinks,
					            knockedOutReactions);

		InvariantInferer inferer = 
			new InvariantInferer(sbaModel, usedReactions);
		
		inferer.computeModelMatrix();
		LinkedList<SimpleTree> trees = new LinkedList<SimpleTree>();
		if (doStateInvariants){
			trees.add(inferer.getStateInvariantTree());
		}
		if (doStateInvariants){
			trees.add(inferer.getUncoveredStateTree());
		}
		if (doActivityInvariants){
			trees.add(inferer.getActivityInvariantTree());
		}
		if (doActivityInvariants){
			trees.add(inferer.getUncoveredActivityTree());
		}
		
		try {
			StringConsumer outWriter = getOutStringConsumer(args);
			for (SimpleTree tree : trees){
				outWriter.append(tree.printTree());
			}
			// outWriter.endLine();
			outWriter.closeStringConsumer();
		} catch (IOException e){
			printUserError ("There was a problem writing the output data");
			printUserError(e.getMessage());
		}
		
	}
	
	
	
	private static void performDoInvariantsCheck (String [] args)
	        throws WrongArgumentsException
	{	
		SBAModel sbaModel = parseAndComputeSBAModel(args);
		
		boolean ignore_sources = commandHasFlag("ignore-sources", args);
		boolean ignore_sinks = commandHasFlag("ignore-sinks", args);
		String[] knockedOutReactions = getKnockedOutReactions(args);
		
		LinkedList<SBAReaction> usedReactions =
			getUsedReactionList(sbaModel,
					            ignore_sources,
					            ignore_sinks,
					            knockedOutReactions);

		InvariantInferer inferer = 
			new InvariantInferer(sbaModel, usedReactions);
		
		inferer.computeModelMatrix();
		
		LinkedList<String> uncovered = inferer.getUncoveredStateStrings();
		boolean mass_conserved = 
			uncovered != null && uncovered.isEmpty();
		String extra_command = 
			"biopepa invariants FILE --ingore-sinks --ignore-sources";
		try {
			StringConsumer outWriter = getOutStringConsumer(args);
			
			if (mass_conserved){
				outWriter.appendLine(
						"Mass appears to be conserved in the model");
			} else {
				outWriter.appendLine("Mass appears not to be conserved");
				outWriter.append("The following components are not involved ");
				outWriter.append("in any invariant, hence may be a good ");
				outWriter.appendLine("place to look for any errors");
				for (String s : uncovered){
					outWriter.appendLine("   " + s);
				}
				outWriter.appendLine(extra_command);
				outWriter.appendLine("may provide you with more information");
				outWriter.appendLine("");
			}
			outWriter.appendLine("Here is the sum of all invariants: ");
			LinkedList<String> lines = inferer.sumOfAllStateInvariants();
			for (String s : lines){
				outWriter.appendLine("   " + s);
			}
			// outWriter.endLine();
			outWriter.closeStringConsumer();
		} catch (IOException e){
			printUserError ("There was a problem writing the output data");
			printUserError(e.getMessage());
		}
		
	}
	private static void performDoVenn (String [] args)
    	throws WrongArgumentsException {	
		SBAModel sbaModel = parseAndComputeSBAModel(args);
		VennInference vennInfer = new VennInference(sbaModel);
		Collection<SimpleTree> vennTrees = vennInfer.inferVennTree();
		try {
			StringConsumer outWriter = getOutStringConsumer(args);
			for (SimpleTree tree : vennTrees){
				outWriter.append(tree.printTree());
			}
			// outWriter.endLine();
			outWriter.closeStringConsumer();
		} catch (IOException e){
			printUserError ("There was a problem writing the output data");
			printUserError(e.getMessage());
		}
	}



	private static void performDistribution (String[] args)
			throws WrongArgumentsException {
		SBAModel sbaModel = parseAndComputeSBAModel(args);
		String targetIdentifier = getTargetIdentifier(args);
		if (targetIdentifier == null || targetIdentifier.equals("")){
			printUserError ("For a distribution you must provide a target");
			printUserError ("identifier with the \"target-comp\" option");
			return;
		}
		if (!commandHasFlag ("target-value", args)){
			printUserError ("For a distribution you must provide a target");
			printUserError ("value with the \"target-value\" option");
			return;
		}
		int targetValue = getTargetValue(args);
		int replications = getIndepentRuns (args, 100);
		
		SimulationTracer simulationTracer = new SimulationTracer (sbaModel);
		double dataPointStep = getDataPointSize(args);
		simulationTracer.setDataPointStep(dataPointStep);
		
		double stopTime = getStopTime (args);
		simulationTracer.setTimeLimit(stopTime);
		
		double [] timepoints;
		double [] cdfPoints;
		try {
			FakeProgressMonitor progressMonitor = new FakeProgressMonitor ();
			simulationTracer.calculateDistribution(targetIdentifier,
					targetValue, replications, progressMonitor);
			timepoints = simulationTracer.getDistributionTimePoints();
			cdfPoints = simulationTracer.getDistributionCdf();
			// pdfPoints = simulationTracer.getDistributionPdf();
		} catch (BioPEPAException e) {
			printSystemError ("There was a problem during simulation: ");
			printSystemError (e.getMessage());
			e.printStackTrace();
			return ;
		} catch (IOException e) {
			printSystemError ("There was a file system problem " +
					"during simulation: ");
			printSystemError (e.getMessage());
			return ;
		}
		
		
		try {
			StringConsumer outWriter = getOutStringConsumer(args);
			cdfToCsv(timepoints, cdfPoints, outWriter);
			outWriter.closeStringConsumer();
		} catch (IOException e){
			printUserError ("There was a problem writing the output data");
			printUserError(e.getMessage());
		}
	}
	
	private static String formatLocation(int line, int column){
		return "Line: " + line + ", Column: " + column ;
	}
	
	private static ModelCompiler parseAndComputeModelCompiler (String[] args) 
			throws WrongArgumentsException {
		String filename = args[0];
		Model astModel = null;
		try {
			astModel = parse(filename);
			List<ReactionTracer> rTracers = getReactionTracers(args);
			for (ReactionTracer rt : rTracers){
				AddReactionTracer.addReactionTracer(astModel, 
						rt.getReactionName(), rt.getTracerName());
			}
		} catch (IOException e) {
			printUserError ("File: " + filename + " not found or unreadable");
			System.exit(1);
		} catch (ParserException e){
			SimpleSourceRange sourceRange = 
				new SimpleSourceRange (e.getChar(), e.getLength(),
						               e.getLine(), e.getColumn());
			printUserError (sourceRange, "There was a parser exception");
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		boolean beVerbose = commandHasFlag("verbose", args);
		if (beVerbose){
			printUserInformation ("parsed okay");
		}
		
		/*
		 * At this point we have the ast model and would like
		 * to substitute in the values which have been overridden.
		 * We may need to split the command-line options into
		 * pre-processing ones and post-processing ones, since we
		 * need the compiled model to, for example return the names
		 * of the components.
		 */
		Map<String, String> overridesMap = obtainOverrides(args);
		try {
			for (Entry<String, String> override : overridesMap.entrySet()){
				String name = override.getKey();
				String value = override.getValue();
				OverrideValueVisitor ovvisitor = new OverrideValueVisitor (name, value);
				astModel.accept(ovvisitor);
				if (ovvisitor.getValueOverridden() == false){
					String warning = 
						"The value for: " + name +
						" given in the overrides has not been overridden. " +
						"This is probably because it does not occur in " +
						"the model";
					printUserWarning(warning);
				}
			}
		} catch (BioPEPAException e1) {
			printUserError("There was an error when overriding values: ");
			printUserError(e1.getMessage());
			System.exit(1);
		}
		
		
		ModelCompiler compiledModel = BioPEPA.compile(astModel);
		
		return compiledModel;
	}
	
	
	private static SBAModel computeSBAModel (ModelCompiler compiledModel, 
				                      		 String[] args) 
								throws WrongArgumentsException {	
		ProblemInfo[] problems = compiledModel.compile();
		int severeProblems = 0;
		
		/*
		 * I'm not quite sure what to do regarding printing the warnings
		 * etc, since we may be using the standard out to get the results
		 * for the webservice. Might have a wee think about this.
		 */
		for (ProblemInfo p : problems){
			if (p.severity.equals(Severity.ERROR)){
				severeProblems++;
				printUserError (p.sourceRange, p.message);
			} else if (!commandHasFlag("no-warnings", args)){
			  printUserWarning (p.sourceRange, p.message);
			}
		}
		SBAModel sbaModel = null;
		if (severeProblems == 0)
			sbaModel = BioPEPA.generateSBA(compiledModel);
		else {
			printUserError ("There were " + severeProblems +
					" severe problems so I cannot continue compilation");
			System.exit(1);
		}
		return sbaModel;
	}	
	
	
	private static SBAModel parseSBMLFile (String filename, String [] args) 
					throws FileNotFoundException, XMLStreamException {
		SBMLReader reader     = new SBMLReader();
		SBMLDocument document = reader.readSBML(filename);
		if (document.getNumErrors() > 0){
			printUserError("Encountered the following errors while " +
					       "reading the SBML file:\n");
			document.printErrors();
			printUserError("\nFurther consistency checking and validation " +
					       "aborted.\n");
			System.exit(1);
		}
		long errors = document.checkConsistency();
		if (errors > 0){
			printUserError("Consistency errors found in SBML model file");
			document.printErrors();
			System.exit(1);
		}
		
		// So if we get here we should have a working SBML model
		
		// TODO: we should create a proper name, filename - .xml
		org.sbml.jsbml.Model sbmlModel = document.createModel("generated-model");
		int numReactions = sbmlModel.getNumReactions();
		printUserInformation ("This model file has " + numReactions + 
				              " reactions");
		
		return null;
	}
	
	private static SBAModel parseAndComputeSBAModel (String[] args) 
							throws WrongArgumentsException {
		String filename = args[0];
		if (filename.endsWith(".xml")){
				try {
					parseSBMLFile(filename, args);
				} catch (FileNotFoundException e) {
					printUserError ("File: " + filename + 
							        " not found or unreadable");
					System.exit(1);
					e.printStackTrace();
				} catch (XMLStreamException e) {
					printUserError ("File: " + filename + 
			        				" causes an xml stream exception (?)");
					e.printStackTrace();
				}
			return null;
		} else {
			ModelCompiler mc = parseAndComputeModelCompiler (args);
			SBAModel sbaModel = computeSBAModel(mc, args);
			return sbaModel;
		}
	}
		
	private static void performTimeSeries (String[] args) 
		throws WrongArgumentsException {
		SBAModel sbaModel = parseAndComputeSBAModel(args);
		Solver solver = getSolverArgument (args);
		
		
		// Now use the chosen solver to calculate the simulation parameters
		Parameters parameters = 
			createSimulationParameters(solver, args, sbaModel);
		
		/* TODO: do something a bit more sensible with the exceptions */
		Result result = null;
		try {
			result = timeSeriesAnalysis(solver, sbaModel, parameters);
		} catch (BioPEPAException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		if (commandHasFlag("showTime", args) ||
				commandHasFlag ("show-time", args)){
			double time = result.getSimulationRunTime();
			printUserInformation ("The simulation took: " + time + " seconds");
		}
		
		if (commandHasFlag("verbose", args)){
			printUserInformation ("Carried out time series analysis okay");
		}
		try {
			StringConsumer outWriter = getOutStringConsumer(args);
			// resultToCsv actually uses a line string builder so we could
			// just pass in the 
			resultToCsv(result, outWriter);
			outWriter.closeStringConsumer();
		} catch (IOException e){
			printUserError ("There was a problem writing the output data");
			printUserError(e.getMessage());
		}
	}
	
	/*
	 * Set the parameters for the simulation/ODE analysis based on
	 * the solver's required parameters plus any overriding options
	 * given on the command-line.
	 */
	private static Parameters createSimulationParameters(Solver solver, 
			String[] args, SBAModel sbaModel) 
				throws WrongArgumentsException{
		Parameters requiredParams = solver.getRequiredParameters();
		Parameters parameters = new Parameters ();
		
		for (Parameter param : requiredParams.arrayOfKeys()){
			parameters.setValue(param, param.getDefault());
		}
		
		String[] componentsToView = sbaModel.getComponentNames();
		String[] variablesToView = sbaModel.getDynamicVariableNames();
		String[] allToView = new String[componentsToView.length + variablesToView.length];
		for (int i = 0; i < componentsToView.length; i++){
			allToView[i] = componentsToView[i];
		}
		for (int i = 0; i < variablesToView.length; i++){
			allToView[i + componentsToView.length] = variablesToView[i];
		}
		parameters.setValue(Parameter.Components, allToView);
		
		
		int runs = getIndepentRuns(args, 1);
		parameters.setValue(Parameter.Independent_Replications, 
								new Integer(runs));
		double startTime = getStartTime(args);
		parameters.setValue(Parameter.Start_Time, new Double(startTime));
		double stopTime = getStopTime(args);
		parameters.setValue(Parameter.Stop_Time, new Double(stopTime));
		double timeStep = getTimeStep(args);
		parameters.setValue(Parameter.Step_Size, new Double(timeStep));
		int dataPoints = getNumberDataPoints(args);
		parameters.setValue(Parameter.Data_Points, dataPoints);

		double relativeError = getRelativeError(args);
		parameters.setValue(Parameter.Relative_Error, relativeError);
		double absoluteError = getAbsoluteError(args);
		parameters.setValue(Parameter.Absolute_Error, absoluteError);
		
		
		
		return parameters;
	}
	
	
	private static Model parse(String filename) throws Exception {
		String source;
		if (filename.equals("stdin")){
		  source = readAllOfStandardIn();
		} else {
		  source = readFileAsString(filename);
		}
		Model astModel = BioPEPA.parse(source);
		
		return astModel;
	}
	
	private static class FakeProgressMonitor implements ProgressMonitor {

		public void beginTask(int amount) {
			// Do nothing
		}

		public void done() {
			// Do nothing
		}

		public boolean isCanceled() {
			return false;
		}

		public void setCanceled(boolean state) {
			// Do nothing
		}

		public void worked(int worked) {
			// Do nothing
		}
		
	}
	
	
	
	private static Result timeSeriesAnalysis (Solver solver, 
			SBAModel sbaModel, Parameters parameters) throws BioPEPAException{
		FakeProgressMonitor progressMonitor = new FakeProgressMonitor ();
		// Take the time now and then we'll do the same when it returns,
		// this might not be incredibly accurate but should at least provide
		// some means of comparison.
		// Get current time 
		long startingTime = System.currentTimeMillis();
		Result results =  solver.startTimeSeriesAnalysis(sbaModel, 
				                              parameters, 
				                              progressMonitor);
		// Get elapsed time in milliseconds 
		long elapsedTimeMillis = System.currentTimeMillis()-startingTime;
		// Get elapsed time in seconds 
		float elapsedTimeSec = elapsedTimeMillis/1000F; 
		// Get elapsed time in minutes 
		// float elapsedTimeMin = elapsedTimeMillis/(60*1000F);
		results.setSimulationRunTime(elapsedTimeSec);
		return results;
	}
	
	/*
	 * A simple method to read an entire file into a String
	 */
	private static String readFileAsString(String filePath) throws java.io.IOException{
	    byte[] buffer = new byte[(int) new File(filePath).length()];
	    BufferedInputStream f = null;
	    try {
	        f = new BufferedInputStream(new FileInputStream(filePath));
	        f.read(buffer);
	    } finally {
	        if (f != null) try { f.close(); } catch (IOException ignored) { }
	    }
	    return new String(buffer);
	}
	
	/*
	 * A simple method to read all of the input from StandardIn, to be
	 * used when we expect the entire model to be read from standard in
	 * as if someone has done:
	 * $ cat myfile.biopepa > biopepa timeseries stdin ....
	 */
	private static String readAllOfStandardIn() { // throws java.io.IOException{
		return StdIn.readAll();
	}
	/*
	 * Turns a simple cdf represented as two double arrays
	 * (one for the time points one for the cdf values) into a
	 * string which can be output to a csv file.
	 * 
	 * The throws declaration is because the string consumer may be 
	 * be a file and there therefore may be some output exception.
	 * If you wish for simply the string representation then:
	 * LineStringBuilder lsb = new LineStringBuilder();
	 * cdfToCsv(timepoints, cdfValues, lsb);
	 * String csvString = lsb.toString();
	 */
	private static void cdfToCsv(double[] timepoints,
			double[] cdfValues, StringConsumer stcon) throws IOException{		
		// Create the header line
		stcon.appendLine("# Time, cdf");
		
		// These should both be the same, but a little robustness
		// rarely hurts.
		int arrayLength = Math.min(timepoints.length, cdfValues.length);
		for (int index = 0; index < arrayLength; index++){
			stcon.append(Double.toString(timepoints[index]));
			stcon.append(",");
			stcon.append(Double.toString(cdfValues[index]));
			stcon.endLine();
		}		
	}
	
	/* This in truth should be placed somewhere nicer it simply turns
	 * a Result into a csv file (string) which can be output to a csv file.
	 * 
	 * The throws declaration is because the string consumer may be 
	 * be a file and there therefore may be some output exception.
	 * If you wish for simply the string representation then:
	 * LineStringBuilder lsb = new LineStringBuilder();
	 * cdfToCsv(timepoints, cdfValues, lsb);
	 * String csvString = lsb.toString();
	 */
	private static void resultToCsv (Result result, StringConsumer stcon) 
		throws IOException{
		
		double[] timepoints = result.getTimePoints();
		String[] components = result.getComponentNames();
		double[][] componentConcentrations = new double[components.length][];
		
		// This kind of assumes that the result is well formed
		// in that there are the same number of time series as there
		// are component names.
		for (int index = 0; index < components.length; index++){
			componentConcentrations[index] = result.getTimeSeries(index);
		}
		
		// Create the header line
	    stcon.append("# Time");
		for (String component : components){
			stcon.append(", " + component);
		}
		stcon.endLine();
		
		// Now for each time point create a single line
		// Again this kind of assumes that the result is well formed
		// in that the length of the time points array is the same as
		// that of each of the time series arrays.
		for (int index = 0; index < timepoints.length; index++){
			stcon.append(Double.toString(timepoints[index]));
			for (double[] timeSeries : componentConcentrations){
				stcon.append(", " + Double.toString(timeSeries[index]));
			}
			stcon.endLine();
		}
	}
	
	/*
	 * Simply prints out to the console, again the idea is that
	 * warnings, errors and information could all be printed out
	 * in a different colour if the terminal supports colour.
	 */
	private static void printUserInformation (String infoString){
		System.out.println (infoString);
	}
	
	/*
	 * Simply prints out to the console a warning for the user.
	 * This warning should be something which we believe is the
	 * user's fault, but which also does not prevent us from
	 * evaluating the model. This might be for example providing an
	 * option to a solver which doesn't accept such an option, eg
	 * if the user selects an ODE solver but then sets the number of
	 * independent replications.
	 * The idea is that we should be able to continue but that the
	 * user might regard the results as suspicious unless they know
	 * what they are doing and can explain the warning.
	 * TODO: can we get different colours for these? 
	 */
	private static void printUserWarning (String warningString){
		System.out.println("Warning:");
		System.out.println(warningString);
		System.out.println("EndWarning:");
	}
	private static void printUserWarning(ISourceRange location, String warning){
		if (location != null){
			int line = location.getLine();
			int column = location.getColumn();
			String posString = formatLocation(line, column);
			printUserWarning(posString + " " + warning);
		} else {
			printUserWarning(warning);
		}
	}
	
	
	/* 
	 * Simply prints to the console an error for the system.
	 * This should be an error that we believe is the users's
	 * fault, such as a parse error in the biopepa file.
	 */
	private static void printUserError(String errorString){
		System.out.println("Error:");
		System.out.println(errorString);
		System.out.println("EndError:");
	}
	private static void printUserError(ISourceRange location, String error){
		if (location != null){
			int line = location.getLine();
			int column = location.getColumn();
			String posString = formatLocation(line, column);
			printUserError(posString + " " + error);
		} else {
			printUserError(error);
		}
	}
	
	/* 
	 * Simply prints to the console an error for the system.
	 * This should be an error that we do not think is the user's
	 * fault.
	 */
	private static void printSystemError(String errorString){
		System.out.println(errorString);
	}
	
}
