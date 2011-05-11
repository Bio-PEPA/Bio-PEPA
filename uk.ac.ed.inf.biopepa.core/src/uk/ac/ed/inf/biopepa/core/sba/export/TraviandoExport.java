package uk.ac.ed.inf.biopepa.core.sba.export;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.interfaces.Exporter;
import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.sba.FileStringConsumer;
import uk.ac.ed.inf.biopepa.core.sba.LineStringBuilder;
import uk.ac.ed.inf.biopepa.core.sba.PhaseLine;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.SBAReaction;
import uk.ac.ed.inf.biopepa.core.sba.StringConsumer;
import uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer.NullTraceLog;
import uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer.SimulationTraceLog;

public class TraviandoExport implements Exporter {
	private static final String description;
	private SBAModel model;
	private int numberFiringsLimit = Integer.MAX_VALUE;
	private double timeLimit = Double.MAX_VALUE;

	private double dataPointStep = 1.0;
	public void setDataPointStep (double newDataPointStep){
		this.dataPointStep = newDataPointStep;
	}	
	
	/* Now for phases */
	/* Now we set up the phases */
	private PhaseLine [] phaseLines;
	public void setPhaseLines (PhaseLine[] phaseLines){
		this.phaseLines = phaseLines;
	}
	

	private boolean displayComments = true;
	private String modelComment = "Trace generated automagically from " + "BioPEPA Eclipse Plugin";

	/*
	 * public TraviandoExport(int numberFiringsLimit, double timeLimit){
	 * this.numberFiringsLimit = numberFiringsLimit; this.timeLimit = timeLimit
	 * ; }
	 */

	static {
		StringBuilder sb = new StringBuilder();
		// This description should be much more verbose
		sb.append("Traviando export");
		description = sb.toString();
	}

	public void setFiringsLimit(int newLimit) {
		this.numberFiringsLimit = newLimit;
	}

	public void setTimeLimit(double newLimit) {
		this.timeLimit = newLimit;
	}

	public void setDisplayComments(boolean b) {
		this.displayComments = b;
	}

	public void setModelComment(String comment) {
		this.modelComment = comment;
	}

	public String canExport() {
		if (model == null)
			throw new IllegalStateException("Model has not been set using setModel/1");
		return null;
	}

	public String getDescription() {
		return description;
	}

	public String getExportPrefix() {
		return "xml";
	}

	public String getLongName() {
		return "Traviandor trace analyser";
	}

	public String getShortName() {
		return "Trav";
	}

	public Object requiredDataStructure() {
		return SBAModel.class;
	}

	public void setModel(SBAModel model) throws UnsupportedOperationException {
		if (model == null)
			throw new NullPointerException("SBA model must be non-null");
		if (this.model != null)
			throw new IllegalStateException("Model has already been set.");
		this.model = model;
	}

	public void setModel(ModelCompiler compiledModel) {
		throw new UnsupportedOperationException();
	}

	private String modelName = null;

	public void setName(String modelName) {
		this.modelName = modelName;
	}

	public Object toDataStructure() throws UnsupportedOperationException {
		throw new UnsupportedOperationException ();
	}

	/*
	 * Here we are turning a model into a traviando trace file.
	 */
	public String toString() {
		if (model == null)
			throw new IllegalStateException("Model has not been set using setModel/1");
		LineStringBuilder travSb = new LineStringBuilder();

		// ISBJava isbJava = new ISBJava(model, model.getComponentNames());
		// SimulationTrace simTrace = isbJava.new SimulationTrace();

		/* I should also be able to make a string consumer which is
		 * outputs to a file and hence doesn't cost as much in memory
		 * hence allowing longer traces.
		 */
		try {
			TraviandoTraceLog traceLog = new TraviandoTraceLog(travSb, model);
			traceLog.setModelName(this.modelName);
			traceLog.setModelComment(this.modelComment);
			traceLog.setDisplayComments(this.displayComments);
			SimulationTracer simTracer = new SimulationTracer(model);
			simTracer.generateSimulationTrace(traceLog);
		} catch (BioPEPAException e) {
			// Todo We should report this better using perhaps
			// a dialog box
			travSb.appendLine(e.getMessage());
		} catch (IOException e){
			travSb.appendLine(e.getMessage());
		}

		return travSb.toString();
	}
	
	private Result simulationResults;
	public Result getSimulationResults(){
		return this.simulationResults;
	}
	/*
	 * Although the 'toString' method above is required for
	 * the 'Exporter' interface, here we allow a more efficient
	 * version which writes directly out to file.
	 */
	public void exportToFile (String filename) throws IOException{
		FileStringConsumer fsc = new FileStringConsumer(filename);
		try {
			fsc.openStringConsumer();
			TraviandoTraceLog traceLog = new TraviandoTraceLog(fsc, model);
			traceLog.setModelName(this.modelName);
			traceLog.setModelComment(this.modelComment);
			traceLog.setDisplayComments(this.displayComments);
			SimulationTracer simTracer = new SimulationTracer(model);
			// Set up the simulation parameters
			simTracer.setTimeLimit(this.timeLimit);
			simTracer.setFiringsLimit(this.numberFiringsLimit);
			simTracer.setDataPointStep(this.dataPointStep);
			simTracer.setPhaseLines(this.phaseLines);
			// Then just let the simulation tracer do its work
			simTracer.generateSimulationTrace(traceLog);
			this.simulationResults = simTracer.getSimulationResults();
		} catch (BioPEPAException e) {
			fsc.appendLine(e.getMessage());
		} 
		fsc.closeStringConsumer();
	}
	/*
	 * Well clearly this is not really appropriate for this class
	 * the intention is that this class is generalised into a
	 * simulation tracer which may not actually produce a file
	 */
	public void justDrawTheGraph () throws IOException {
		try {
			SimulationTracer simTracer = new SimulationTracer(model);
			NullTraceLog traceLog = new NullTraceLog();
			
			// Set up the simulation parameters
			simTracer.setTimeLimit(this.timeLimit);
			simTracer.setFiringsLimit(this.numberFiringsLimit);
			simTracer.setDataPointStep(this.dataPointStep);
			simTracer.setPhaseLines(this.phaseLines);
			// Then just let the simulation tracer do its work
			simTracer.generateSimulationTrace(traceLog);
			this.simulationResults = simTracer.getSimulationResults();
		} catch (BioPEPAException e) {
			return;
		} 
	}
	
	
	
	public static class TraviandoTraceLog implements SimulationTraceLog {
		private StringConsumer scon;
		/*
		 * If this trace is a file (or other output) of its
		 * own then it should open and close the string consumer.
		 * However this trace may be part of a larger output and
		 * hence should not manage the open and closing of the file
		 * consumer.
		 */
		private boolean completeConsumer = true;
		private HashMap<String, String> compVarNames;
		private HashMap<String, String> reactionIdMap;
		private HashMap<String, String> compIdMap;
		private String [] componentNames;
		private SBAReaction[] sbaReactions;
		private String modelName = "";
		private String modelComment = "";
		private boolean displayComments = false;
		/*
		 * If this trace is a file (or other output) of its
		 * own then it should open and close the string consumer.
		 * However this trace may be part of a larger output and
		 * hence should not manage the open and closing of the file
		 * consumer. Hence in the latter case the caller should call
		 * setCompleteConsumer(false) and manage the string consumer
		 * themselves.
		 */
		public void setCompleteConsumer (boolean complete){
			this.completeConsumer = complete;
		}
		
		public TraviandoTraceLog (StringConsumer scon, SBAModel model){
			this.scon = scon;
			this.compVarNames = new HashMap<String, String>();
			this.reactionIdMap = new HashMap<String, String>();
			this.compIdMap = new HashMap<String, String>();
			this.componentNames = model.getComponentNames();
			this.sbaReactions = model.getReactions();
			
		}
		
		public void setModelName (String name){
			this.modelName = name;
		}
		public void setModelComment (String comment){
			this.modelComment = comment;
		}
		public void setDisplayComments (boolean display){
			this.displayComments = display;
		}
		
		public void traceLogFooter (Result result) throws IOException {
			scon.appendLine("</Sequence>");
			scon.appendLine("</Trace>");
			if (this.completeConsumer){
				scon.closeStringConsumer();
			}
		}
		
		public void traceLogHeader(HashMap<String, Number> componentCounts) 
			throws IOException {
			if (this.completeConsumer){
				scon.openStringConsumer();
			}
			scon.appendLine("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
			scon.appendLine("<!-- Trace file of model " + modelName + " -->");
			scon.appendLine("<Trace model=\"" + modelName + "\" generator=\"BioPEPA-Eclipse-Plugin\" >");
			scon.appendLine("<Comment> " + modelComment + " </Comment>");
			scon.appendLine("<!-- declaration of processes -->");

			/*
			 * Here we say that each population is a process which has one
			 * variable which is its own population count. This may be a bit
			 * redundant but I don't think you can assign to a process. This
			 * means the var name is the same as the Process name, but I think
			 * that's actually correct.
			 */
			for (int i = 0; i < componentNames.length; i++) {
				String procName = componentNames[i];
				scon.appendLine("<Process id=\"" + i + "\" name=\"" + procName + "\" >");

				/*
				 * Now we wish to know if we should output any (private) actions
				 * for this process, so we iterate through all the reactions and
				 * for each reaction if it is the sole product or reactant then
				 * we output the reaction as a private action of this process
				 */
				for (int index = 0; index < sbaReactions.length; index++) {
					SBAReaction reaction = sbaReactions[index];
					// appendLine(sb, "Doing reaction - " + reaction.getName());
					boolean containsThis = false;
					boolean containsOther = false;
					// appendLine(sb, "Doing the reaction: " +
					// reaction.getName());
					for (SBAComponentBehaviour product : reaction.getProducts()) {
						String pName = product.getName();
						if (procName.equals(pName)){
							containsThis = true;
						} else {
							containsOther = true;
						}
					}
					for (SBAComponentBehaviour reactant : reaction.getReactants()) {
						if (procName.equals(reactant.getName())){
							containsThis = true;
						} else {
							containsOther = true;
						}
					}
					// appendLine(sb, "End reaction: " + reaction.getName());
					if (containsThis && !containsOther) {
						String idName = "a_" + index;
						String rName = reaction.getName();
						scon.appendLine("<Action id=\"" + idName + "\" name =\"" + rName + "\" />");
						reactionIdMap.put(rName, idName);
					}
				}

				/*
				 * Now we output the 'Var' description which basically just
				 * declares a variable associated with the process the
				 * associated variable contains the population count.
				 */
				compIdMap.put(procName, Integer.toString(i));
				String popName = "pop_" + i;
				scon.appendLine("<Var id = \"" + popName + "\" name=\"" + procName + "\" />");
				scon.appendLine("</Process>");
				compVarNames.put(procName, popName);
			}

			/*
			 * Now the interactions basically for each reaction we state that a
			 * component is 'touched' by it, if it is either a product or a
			 * reactant
			 */
			scon.appendLine("<Interactions>");
			// ExperimentLine experimentLine = new ExperimentSet().emptyExperimentLine("default");
			for (int index = 0; index < sbaReactions.length; index++) {
				SBAReaction reaction = sbaReactions[index];
				List<SBAComponentBehaviour> products = reaction.getProducts();
				List<SBAComponentBehaviour> reactants = reaction.getReactants();
				String reactionId = "sa_" + index;
				/*
				 * This test kind of assumes we don't have a single component
				 * which is both a product and a reactant
				 */
				if (products.size() + reactants.size() > 1) {
					scon.appendLine("<Undiraction id=\"" + reactionId + "\" name=\"" + reaction.getName() + "\" >");
					reactionIdMap.put(reaction.getName(), reactionId);
					for (SBAComponentBehaviour p : products) {
						scon.appendLine("<Touch>" + compIdMap.get(p.getName()) + "</Touch>");
					}
					for (SBAComponentBehaviour r : reactants) {
						scon.appendLine("<Touch>" + compIdMap.get(r.getName()) + "</Touch>");
					}
					scon.appendLine("</Undiraction>");
				}
			}

			scon.appendLine("</Interactions>");

			scon.appendLine("<Sequence type=\"not-sure\">");
			scon.appendLine("<S>");
			for (String compName : componentNames) {
				String compId = compVarNames.get(compName);
				Number compCount = componentCounts.get(compName);
				outputAssignment(compId, compCount.intValue());
			}
			scon.appendLine("</S>");

		}
		
		private void outputComponentCounts(HashMap<String, Number> componentCounts) throws IOException {
			scon.appendLine(" <!-- ");
			for (Entry<String, Number> entry : componentCounts.entrySet()) {
				scon.appendLine("Component: " + entry.getKey() + " = " + entry.getValue());
			}
			scon.appendLine(" --> ");
		}

		/* Output the initial population counts */
		private void outputAssignment(String procName, int count) throws IOException {
			scon.appendLine("<V id=\"" + procName + "\" val=\"" + count + "\" />");
		}
		
		public void displayComponentCounts (HashMap<String, Number> componentCounts) 
			throws IOException{
			if (displayComments) {
				outputComponentCounts(componentCounts);
			}
		}
		
		public void displayEnabledReaction(String reactionName, double rateValue) throws IOException {
			if (displayComments) {
				scon.appendLine(" <!-- " + reactionName + " has rate " + rateValue + " (which is : "
						+ /*rValue.toString() + */ ") --> ");
			}
		}
		/*
		 * (non-Javadoc)
		 * @see uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer.SimulationTraceLog#startEvent(java.lang.String, double)
		 * The arguments to startEvent and stopEvent are set up for traviando traces,
		 * we should either pass all information into each of them or find a better way to
		 * log events.
		 */
		public void startEvent(String rname, double totalTime) throws BioPEPAException, IOException{
			String idName = reactionIdMap.get(rname);
			if (idName == null){
				throw new BioPEPAException ("Reaction: " + rname + 
						" not found in reaction id map");
			}
			scon.appendLine("<A id=\"" + idName + "\" t=\"" + totalTime + "\"" + " i=\""
					+ rname + ".1\" >");
		}
		
		public void outputComponentUpdate (String rName, int newValue) throws IOException {
			outputAssignment(compVarNames.get(rName), newValue);
		}
		
		public void endEvent(double thisDelay, double totalRate, 
				HashMap<String, Number> componentCounts)
				throws IOException {
			if (displayComments) {
				scon.appendLine(" <!-- This firing's delay is: " + thisDelay + " --> ");
				scon.appendLine(" <!-- The rate is: " + totalRate + " --> ");
				outputComponentCounts(componentCounts);
			}

			scon.appendLine("</A>");
		}
		public void reportDeadlocked () throws IOException {
			scon.appendLine("<!-- Total rate = zero, deadlocked state -->");
		}
	}
	

}
