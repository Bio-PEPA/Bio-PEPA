/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.dialogs.SaveAsDialog;

import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.interfaces.Solver;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentSet;
import uk.ac.ed.inf.biopepa.core.sba.Parameters;
import uk.ac.ed.inf.biopepa.core.sba.PhaseLine;
import uk.ac.ed.inf.biopepa.core.sba.Solvers;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentLine;
import uk.ac.ed.inf.biopepa.core.sba.Parameters.Parameter;
import uk.ac.ed.inf.biopepa.ui.BioPEPAEvent;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;
import uk.ac.ed.inf.biopepa.ui.interfaces.IResourceProvider;

/**
 * 
 * @author ajduguid
 * 
 */
public class TimeSeriesAnalysisWizard extends Wizard implements IResourceProvider {

	BioPEPAModel model;

	Solver solver;

	Map<Parameter, Object> uParameters;

	Parameters parameters;

	private SpeciesSelectionWizardPage speciesSelectionPage;
	private ImportCSVPage importCSVPage;
	private ImportDataPage importDataPage;
	private PhasesPage phasesPage;

	public TimeSeriesAnalysisWizard(BioPEPAModel model) {
		if (model == null)
			throw new NullPointerException("Error; model does not exist.");
		this.model = model;
		setHelpAvailable(false);
		setNeedsProgressMonitor(true);
		setWindowTitle("Time-series analysis wizard");
		uParameters = new HashMap<Parameter, Object>();
		loadParameters();
	}

	public void addPages() {
		speciesSelectionPage = new SpeciesSelectionWizardPage(model, uParameters);
		addPage(speciesSelectionPage);
		addPage(new AlgorithmWizardPage(model, uParameters));
		importCSVPage = new ImportCSVPage(model);
		addPage(importCSVPage);
		phasesPage = new PhasesPage(model);
		addPage(phasesPage);
		importDataPage = new ImportDataPage("Import Experimental Data");
		addPage(importDataPage);
	}

	/*
	 * We needn't write our own 'canFinish' because the default just checks if
	 * all pages are complete which is what we want.
	 */

	public IResource getUnderlyingResource() {
		return model.getUnderlyingResource();
	}

	@Override
	public boolean performFinish() {
		/*
		 * String name = model.getUnderlyingResource().getName(); Export sbml =
		 * new SBMLExport(model.getSBAModel(), SBMLExport.flattenName(name));
		 * IPath path =
		 * model.getUnderlyingResource().getFullPath().removeFileExtension();
		 * path = path.addFileExtension("xml"); IFile file =
		 * ResourcesPlugin.getWorkspace().getRoot().getFile(path); InputStream
		 * source = new ByteArrayInputStream(sbml.toString().getBytes()); try {
		 * if(file.exists()) file.setContents(source, true, false, null); else
		 * file.create(source, true, null); } catch(Exception e) {}
		 */
		parameters.setValue(Parameter.Components, uParameters.get(Parameter.Components));
		saveParameters();
		// prepare simulation and creation of graphs
		model.notify(new BioPEPAEvent(model, BioPEPAEvent.Event.MODIFIED, 1));

		// Set up the experiment from the csv import page.
		ExperimentSet experSet;
		experSet = importCSVPage.getExperimentSet();
		if (experSet == null) {
			experSet = new ExperimentSet();
			experSet.addExperimentLine(new ExperimentLine("results"));
		}
		experSet.setSeparateGraphs(importCSVPage.getSeparateGraphs());
		Result externalResult = importDataPage.getPlottableResult();
		if (externalResult != null){
			ExperimentLine externalLine = new ExperimentLine("ext-data");
			externalLine.setResult(externalResult);
			experSet.addExperimentLine(externalLine);
		}

		// Set up the analysis job
		AnalysisJob myJob = new AnalysisJob(model, parameters, experSet, solver);

		// Now from the import csv page we must decide if we are
		// only going to output directly to csv
		boolean onlyCsv = importCSVPage.getJustBuildCsv();
		myJob.setJustBuildCsv(onlyCsv);
		
		// Now determine if any phases were set up
		PhaseLine[] phaseLines = phasesPage.getPhaseLines();
		if (phaseLines != null){
			myJob.setPhaseLines(phaseLines);
		}

		// If we are only building straight to csv and not displaying
		// the graph then we need to ask for a csv file this path will
		// be modified in the case that there are multiple graphs.
		if (onlyCsv) {
			SaveAsDialog saveAsDialog = new SaveAsDialog(getShell());
			IPath path = model.getUnderlyingResource().getFullPath();
			path = path.removeFileExtension().addFileExtension("csv");
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			saveAsDialog.setOriginalFile(file);
			saveAsDialog.open();
			path = saveAsDialog.getResult();
			myJob.setCsvPath(path);
		}

		myJob.schedule();

		return true;
	}

	private void loadParameters() {
		Class<?> c = (new String[] {}).getClass();
		Parameter[] pArray = Parameter.values();
		String s;
		Object o;
		solver = Solvers.getSolverInstance(model.getProperty("solver"));
		outer: for (Parameter p : pArray) {
			s = model.getProperty(p.getKey());
			if (s == null)
				continue outer;
			o = p.parseString(s);
			if (o != null)
				uParameters.put(p, o);
			else if (p.getType().equals(c)) {
				// bencoded
				StringBuilder sb = new StringBuilder((String) s);
				ArrayList<String> al = new ArrayList<String>();
				int index, length;
				try {
					while (sb.length() > 0) {
						index = sb.indexOf(":");
						if (index == -1)
							continue outer;
						length = Integer.parseInt(sb.substring(0, index));
						sb.delete(0, index + 1);
						al.add(sb.substring(0, length));
						sb.delete(0, length);
					}
				} catch (NumberFormatException e) {
					continue;
				}
				uParameters.put(p, al.toArray(new String[] {}));
			}
		}
	}

	private void saveParameters() {
		Class<?> c = (new String[] {}).getClass();
		if (solver != null)
			model.setProperty("solver", solver.getShortName());
		for (Map.Entry<Parameter, Object> me : uParameters.entrySet()) {
			if (me.getKey().getType().equals(c) && me.getValue() != null) {
				String[] sa = (String[]) me.getValue();
				StringBuilder sb = new StringBuilder();
				for (String s : sa)
					sb.append(s.length()).append(":").append(s);
				model.setProperty(me.getKey().getKey(), sb.toString());
			} else if (me.getValue() != null)
				model.setProperty(me.getKey().getKey(), me.getValue().toString());
		}
	}

}
