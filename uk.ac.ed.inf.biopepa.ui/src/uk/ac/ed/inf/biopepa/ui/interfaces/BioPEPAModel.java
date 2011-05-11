/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.interfaces;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.interfaces.Solver;
import uk.ac.ed.inf.biopepa.core.sba.Parameters;
import uk.ac.ed.inf.biopepa.core.sba.PhaseLine;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentLine;
import uk.ac.ed.inf.biopepa.ui.BioPEPAEvent;

public interface BioPEPAModel extends IResourceProvider {
	
	public void addListener(BioPEPAListener listener);
	
	public void notify(BioPEPAEvent event) ;
	
	public void dispose();
	
	public String[] getComponentNames();
	
	public String[] getDynamicVariableNames();
	
	public String getProperty(String name);
		
	public void parse() throws CoreException;
	
	public void overrideAndRecompile(ExperimentLine experimentLine) throws BioPEPAException;
	
	public SBAModel getSBAModel();
	
	public ModelCompiler getCompiledModel();
	
	public void removeListener(BioPEPAListener listener);
	
	public void setProperty(String name, String value);
	
	public Result runPhasesTimeSeries(Solver solver,
			Parameters parameters,
			IProgressMonitor monitor,
			PhaseLine[] phaseLines) throws BioPEPAException, CoreException;
	public Result timeSeriesAnalysis(Solver solver, 
			                         Parameters parameters,
			                         IProgressMonitor monitor) throws CoreException;
	
	public boolean errorsPresent();

}
