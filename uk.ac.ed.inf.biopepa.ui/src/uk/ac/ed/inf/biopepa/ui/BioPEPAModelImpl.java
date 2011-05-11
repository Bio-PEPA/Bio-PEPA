/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui;

import java.io.*;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;

import uk.ac.ed.inf.biopepa.core.BioPEPA;
import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.compiler.ProblemInfo;
import uk.ac.ed.inf.biopepa.core.compiler.ProblemInfo.Severity;
import uk.ac.ed.inf.biopepa.core.dom.Model;
import uk.ac.ed.inf.biopepa.core.dom.internal.ParserException;
import uk.ac.ed.inf.biopepa.core.interfaces.*;
import uk.ac.ed.inf.biopepa.core.sba.Parameters;
import uk.ac.ed.inf.biopepa.core.sba.Parameters.Parameter;
import uk.ac.ed.inf.biopepa.core.sba.PhaseLine;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentLine;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAListener;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;

public class BioPEPAModelImpl implements BioPEPAModel {
	Model astModel = null;
	ModelCompiler compiledModel = null;
	private List<BioPEPAListener> listeners = new ArrayList<BioPEPAListener>();
	Map<QualifiedName, String> metaData = new HashMap<QualifiedName, String>();
	ProblemInfo[] problems;
	IResource resource = null;
	List<IMarker> markers = new ArrayList<IMarker>();

	Runnable bringProblemsToView = new Runnable() {
		public void run() {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().showView(IPageLayout.ID_PROBLEM_VIEW);
			} catch (Exception e) {
			}
		}
	};

	SBAModel sbaModel = null;

	BioPEPAModelImpl(IResource resource) {
		this.resource = resource;
	}

	public void addListener(BioPEPAListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void dispose() {
		for (Map.Entry<QualifiedName, String> me : metaData.entrySet()) {
			try {
				resource.setPersistentProperty(me.getKey(), me.getValue());
			} catch (CoreException e) {
			}
		}
		try {
			for (IMarker marker : markers)
				marker.delete();
		} catch (CoreException e) {
		}
	}

	public String[] getComponentNames() {
		if (sbaModel != null)
			return sbaModel.getComponentNames();
		return null;
	}

	public String[] getDynamicVariableNames() {
		if (sbaModel != null)
			return sbaModel.getDynamicVariableNames();
		return new String[0];
	}

	public String getProperty(String name) {
		QualifiedName key = new QualifiedName(BioPEPAPlugin.PLUGIN_ID, name);
		if (metaData.containsKey(key))
			return metaData.get(key);
		String s = null;
		try {
			s = resource.getPersistentProperty(key);
			metaData.put(key, s);
		} catch (CoreException e) {
			s = null;
		}
		return s;
	}

	public IResource getUnderlyingResource() {
		return resource;
	}

	private String source = null;
	public void parse() throws CoreException {
		long start = System.currentTimeMillis();
		for (IMarker marker : markers)
			marker.delete();
		markers.clear();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					((IFile) resource).getContents()));
			StringBuffer buf = new StringBuffer();
			char[] cbuf = new char[4096];
			int c;
			while ((c = in.read(cbuf)) != -1) {
				buf.append(cbuf, 0, c);
			}
			source = buf.toString();
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, BioPEPAPlugin.PLUGIN_ID,
					"Input/Output problem", e);
			BioPEPAPlugin.getDefault().log(status);
			Display.getDefault().asyncExec(bringProblemsToView);
			throw new CoreException(status);
		}
		if (source != null) {
			try {
				astModel = BioPEPA.parse(source);
			} catch (Throwable e) {
				IStatus status = new Status(IStatus.ERROR,
						BioPEPAPlugin.PLUGIN_ID, "Parsing problem", e);
				if (e instanceof ParserException) {
					ParserException pe = (ParserException) e;
					IMarker marker = resource.createMarker(IMarker.PROBLEM);
					marker.setAttribute(IMarker.MESSAGE, pe.getMessage());
					marker.setAttribute(IMarker.LINE_NUMBER, pe.getLine());
					marker.setAttribute(IMarker.CHAR_START, pe.getChar());
					marker.setAttribute(IMarker.CHAR_END, pe.getChar()
							+ pe.getLength());
					marker.setAttribute(IMarker.SEVERITY,
							IMarker.SEVERITY_ERROR);
					marker.setAttribute(IMarker.TRANSIENT, true);
					markers.add(marker);
				} else
					BioPEPAPlugin.getDefault().log(status);
				Display.getDefault().asyncExec(bringProblemsToView);
				throw new CoreException(status);
			}
		}
		compiledModel = BioPEPA.compile(astModel);
		problems = compiledModel.compile();
		int c = 0;
		for (ProblemInfo p : problems)
			if (p.severity.equals(Severity.ERROR))
				c++;
		if (c == 0)
			sbaModel = BioPEPA.generateSBA(compiledModel);
		else {
			Display.getDefault().asyncExec(bringProblemsToView);
			sbaModel = null;
		}
		IMarker marker;
		for (ProblemInfo pi : problems) {
			marker = resource.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.MESSAGE, pi.message);
			marker.setAttribute(IMarker.SEVERITY, eclipseSeverity(pi.severity));
			marker.setAttribute(IMarker.TRANSIENT, true);
			if (pi.sourceRange != null) {
				marker.setAttribute(IMarker.LINE_NUMBER, pi.sourceRange
						.getLine());
				marker.setAttribute(IMarker.CHAR_START, pi.sourceRange
						.getChar());
				marker.setAttribute(IMarker.CHAR_END, pi.sourceRange.getChar()
						+ pi.sourceRange.getLength());
			}
			markers.add(marker);
		}
		notify(new BioPEPAEvent(this, BioPEPAEvent.Event.PARSED, System
				.currentTimeMillis()
				- start));
	}

	/*
	 * Used for overriding during experimentation, we assume that
	 * the problems introduced by the experimentation are not interesting
	 * for the user, so for example when we knock out a reaction this will
	 * give the reaction a rate of zero which in turn means that it will
	 * not be dependent on the concentrations of the reactants. But this
	 * warning would be uninteresting.
	 * 
	 * A further concern is that the model may have already been overridden,
	 * we wish to avoid the problem of accumulating overrides. For this reason
	 * we hold a copy of the original model's source code and 
	 * we unfortunately need to re-parse the source code to produce a copy of
	 * the astModel which we are free to modify.
	 * Alternatively we could implement cloning for ASTModels.
	 * 
	 */
	public void overrideAndRecompile (ExperimentLine experimentLine) 
			throws BioPEPAException{
		if (astModel != null && this.source != null){
			try {
				astModel = BioPEPA.parse(source);
			} catch (ParserException e) {
				throw new BioPEPAException ("Original source not-reparsable, fatal error");
			} catch (Exception e) {
				throw new BioPEPAException ("Original source not-reparsable, fatal error");
			}
			experimentLine.applyToAst(astModel);
			compiledModel = BioPEPA.compile(astModel);
			problems = compiledModel.compile();
			sbaModel = BioPEPA.generateSBA(compiledModel);
		}
	}
	
	public void notify(BioPEPAEvent event) {
		for (BioPEPAListener l : listeners)
			l.modelChanged(event);
	}

	private static final int eclipseSeverity(Severity severity) {
		switch (severity) {
		case ERROR:
			return IMarker.SEVERITY_ERROR;
		case WARNING:
			return IMarker.SEVERITY_WARNING;
		case INFO:
			return IMarker.SEVERITY_INFO;
		default:
			throw new UnsupportedOperationException();
		}
	}

	public void removeListener(BioPEPAListener listener) {
		listeners.remove(listener);
	}

	public void setProperty(String name, String value) {
		QualifiedName key = new QualifiedName(BioPEPAPlugin.PLUGIN_ID, name);
		metaData.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel#runPhasesTimeSeries(uk.ac.ed.inf.biopepa.core.interfaces.Solver, uk.ac.ed.inf.biopepa.core.sba.Parameters, org.eclipse.core.runtime.IProgressMonitor, uk.ac.ed.inf.biopepa.core.sba.ExperimentSet.ExperimentLine[], double[])
	 * TODO: There is a slight problem here, we need to ensure that every
	 * single component is in the results, otherwise it won't get added to
	 * the phase line  in order to override its initial population.
	 * So we need someway of adding all the components and then triming
	 * the results at the end.
	 */
	public Result runPhasesTimeSeries(Solver solver,
			Parameters parameters,
			IProgressMonitor monitor,
			PhaseLine[] phaseLines) throws BioPEPAException, CoreException {
		double endTime = (Double) parameters.getValue(Parameter.Stop_Time);
		Result result = null;
		double processedTime = 0.0;
		
		int phaseIndex = 0;
		while (processedTime < endTime){
			PhaseLine phaseLine = phaseLines[phaseIndex];
			ExperimentLine experLine = phaseLine.getExperimentLine();
			// If there is already a result then we should 
			// override the initial populations with the current
			// populations.
			if (result != null){
				String [] componentNames = result.getComponentNames();
				for (int index = 0; index < componentNames.length; index++){
					String name = componentNames[index];
					double midwayPopulation = result.getPopulation(index);
					experLine.addInitialConcentration(name, midwayPopulation);
				}
			}
			this.overrideAndRecompile(experLine);
			double thisDelay = phaseLine.getDuration();
			parameters.setValue(Parameter.Stop_Time, thisDelay);
			Result thisResult = timeSeriesAnalysis(solver, parameters, monitor);
			
			if (result == null){
				result = thisResult;
			} else {
				result.concatenateResults(thisResult);
			}
			
			
			phaseIndex++;
			if (phaseIndex >= phaseLines.length){
				phaseIndex = 0;
			}
			processedTime += thisDelay;
		}
		
		return result;
		
	}
	
	public Result timeSeriesAnalysis(Solver solver, 
			Parameters parameters,
			IProgressMonitor monitor) throws CoreException {
		if (solver == null || parameters == null)
			throw new NullPointerException();
		try {
			ProgressMonitor progressMonitor =
			   (monitor == null ? null : 
				   new ProgressMonitorImpl("", monitor));
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
		} catch (BioPEPAException e) {
			IStatus status = new Status(IStatus.ERROR, BioPEPAPlugin.PLUGIN_ID,
					e.getMessage());
			// Used to log error here, but current code extends Job which logs
			// non-OK status responses itself.
			// BioPEPAPlugin.getDefault().log(status);
			throw new CoreException(status);
		}
	}

	public boolean errorsPresent() {
		try {
			for (IMarker marker : markers)
				if (((Integer) marker.getAttribute(IMarker.SEVERITY))
						.intValue() == IMarker.SEVERITY_ERROR)
					return true;
		} catch (CoreException e) {
			return true;
		}
		return false;
	}

	public SBAModel getSBAModel() {
		return sbaModel;
	}

	public ModelCompiler getCompiledModel() {
		return compiledModel;
	}
}
