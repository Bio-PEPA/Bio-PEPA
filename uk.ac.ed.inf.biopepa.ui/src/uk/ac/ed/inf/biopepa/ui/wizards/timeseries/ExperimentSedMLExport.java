package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import java.util.Set;
import java.util.Map.Entry;

import org.sedml.ChangeAttribute;
import org.sedml.Model;
import org.sedml.Output;
import org.sedml.Plot2D;
import org.sedml.SEDMLDocument;
import org.sedml.SedML;
import org.sedml.Simulation;
import org.sedml.UniformTimeCourse;
import org.sedml.modelsupport.SUPPORTED_LANGUAGE;

import uk.ac.ed.inf.biopepa.core.sba.ExperimentSet;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentLine;

public class ExperimentSedMLExport {
	private ExperimentSet experimentSet;
	private SBAModel sbaModel;
	
	public ExperimentSedMLExport (ExperimentSet es, SBAModel model){
		this.experimentSet = es;
		this.sbaModel = model;
	}
	
	public SEDMLDocument exportToSedML (){
		SEDMLDocument sedMLDoc = new SEDMLDocument();
		SedML sedml = sedMLDoc.getSedMLModel();
		// SBMLExport sbmlExport = new SBMLExport ();
		// sbmlExport.setModel(this.model.getSBAModel());
		// String modelSource = sbmlExport.toString();
		
		// TODO: should take the simulation etc from the settings,
		// which means we will have to pass them in here.
		Simulation sim = new UniformTimeCourse("id", "name", 0, 0, 10, 100);
		sedml.addSimulation(sim);
		
		int line = 0;
		for (ExperimentLine eline : experimentSet.getExperimentLines()){
			line++;
			// If the model has the same id each time around then
			// subsequent models will not get added so we attach the
			// experiment line number.
			Model sedmlModel = new Model ("NoID_" + line, 
					"mymodel", 
					SUPPORTED_LANGUAGE.SBML_GENERIC.getURN(),
					// Hmm we don't actually output the model to
					// this so this is 'dodgy' at best.
					"model.xml");
			Set<Entry<String, Number>> entries = eline.getInitialPopulations().entrySet();
			for (Entry<String, Number> override : entries){
				ChangeAttribute change = new ChangeAttribute(override.getKey(),
						                                     override.getValue().toString());
				System.out.println ("Adding pop: " + override.getKey() + " : " + override.getValue().toString());
				if (sedmlModel.addChange(change)){
					System.out.println("Change added successfully");
				} else {
					System.out.println ("Change not added successfully");
				}
			}
			entries = eline.getRateValues().entrySet();
			for (Entry<String, Number> override : entries){
				ChangeAttribute change = new ChangeAttribute(override.getKey(),
						                                     override.getValue().toString());
				System.out.println ("Adding rate value: " + override.getKey() + " : " + override.getValue().toString());

				sedmlModel.addChange(change);
			}
			Set<Entry<String, Boolean>> reactionEntries = eline.getReactionActivations().entrySet();
			for (Entry<String, Boolean> entry : reactionEntries){
				ChangeAttribute change = new ChangeAttribute(entry.getKey(),
															 entry.getValue().toString());
				System.out.println ("Adding reaction act: " + entry.getKey() + " : " + entry.getValue().toString());

				sedmlModel.addChange(change);
			}
			sedml.addModel(sedmlModel);
		}
					
		Output thisOutput = new Plot2D ("NoID", "myoutputname");
		sedml.addOutput(thisOutput);
		
		
		// Should we validate before returning?
		return sedMLDoc;
	}

}
