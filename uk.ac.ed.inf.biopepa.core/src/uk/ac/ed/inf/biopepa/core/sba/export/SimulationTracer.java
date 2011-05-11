package uk.ac.ed.inf.biopepa.core.sba.export;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.ac.ed.inf.biopepa.core.BasicResult;
import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpression;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpressionEvaluator;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpressionRateEvaluator;
import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;
import uk.ac.ed.inf.biopepa.core.interfaces.ProgressMonitor;
import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentLine;
import uk.ac.ed.inf.biopepa.core.sba.PhaseLine;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour.Type;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.SBAReaction;


public class SimulationTracer {
	/* Slightly risky setting these so high but the
	 * point is that they should always be set manually
	 * before actually starting the simulation.
	 */
	private int numberFiringsLimit = Integer.MAX_VALUE;
	private double timeLimit = Double.MAX_VALUE;
	private SBAModel sbaModel;
	
	public SimulationTracer (SBAModel model){
		this.sbaModel = model;
	}
	
	public void setTimeLimit (double newTimeLimit){
		this.timeLimit = newTimeLimit;
	}
	public void setFiringsLimit(int newLimit) {
		this.numberFiringsLimit = newLimit;
	}

	// sample from the exponential distribution
	private double expDelay(double mean) {
		return -mean * Math.log(generator.nextDouble());
	}
	
	
	/* This is the simulation trace code */
	private Random generator = new Random();

	
	
	private interface TraceResults extends Result {
		/*
		 * Should return true if the simulation is to continue
		 * and false if we should stop the simulation.
		 */
		public boolean updateResults (double newTotalTime, HashMap<String, Number> componentCounts);
		public void completeDeadLock (HashMap<String, Number> componentCounts);
		/*
		 * Combine the given result into the current result
		 * creating an average of the two, note that either may have
		 * been an average in the 
		 */
		public void aggregateResults (TraceResults newResult);
		public int numberOfAggregatedResults ();
	}
	
	/*
	 * A basic implementation of the above TraceResults signature
	 * which simply extends that of BasicResult. This means that we
	 * require to know ahead of time how many time points there will be.
	 */
	private class TraceResultsFixedTime extends BasicResult implements TraceResults {
		protected int ourTimeIndex;
		
		int numberOfResults;
		
		public int numberOfAggregatedResults (){
			return this.numberOfResults;
		}
		
		public void aggregateResults (TraceResults extraResults){
			int numberExtraResults = extraResults.numberOfAggregatedResults();
			int totalNumberResults = this.numberOfResults + numberExtraResults;
			
			/* We assume that the component names are the same
			 * and that the time points are the same.
			 */
			for (int nameIndex = 0; nameIndex < results.length; nameIndex++){
				double [] theseResults = results[nameIndex];
				double [] newResults = extraResults.getTimeSeries(nameIndex);
				for (int timeIndex = 0; timeIndex < theseResults.length; timeIndex++){
					double newValue = ((theseResults[timeIndex] * this.numberOfResults) +
					                   (newResults[timeIndex] * numberExtraResults)) / 
					                   totalNumberResults;
					results[nameIndex][timeIndex] = newValue;
				}
			}
			
			
			this.numberOfResults = totalNumberResults;
		}
		
		TraceResultsFixedTime (String[] compNames, double startTime){
			this.componentNames = compNames;
			int timepointsSize = 0 ;
			
			// First work out how many time points there will be
			double fakeTime = startTime; 
			while (fakeTime < timeLimit){
				timepointsSize++;
				fakeTime += dataPointStep;
			}
			// Then create the time points array and fill it with the
			// times in question.
			this.timePoints = new double[timepointsSize];
			fakeTime = startTime;
			for (int index = 0; index < timepointsSize; index++){
				this.timePoints[index] = fakeTime;
				fakeTime += dataPointStep;
			}
			
			// For each component name create a results array the same
			// length as the time points array
			this.results = new double[componentNames.length][];
			for (int i = 0; i < componentNames.length; i++){
				this.results[i] = new double[timepointsSize];
			}
			
			// this.ourTime      = 0.0;
			this.ourTimeIndex = 0;
			this.numberOfResults = 1;
		}
		
		public boolean updateResults (double newTotalTime, 
				HashMap<String, Number> componentCounts){
			while (ourTimeIndex < timePoints.length && 
					timePoints[ourTimeIndex] <= newTotalTime){
				for (int index = 0; index < componentNames.length; index++){
					// TODO: probably should check if this comes back with null
					double thisValue = componentCounts.get(componentNames[index]).doubleValue();
					results[index][ourTimeIndex] = thisValue;
				}
				ourTimeIndex++;
			}
			// We always let the simulation continue
			return true;
		}
		
		public void completeDeadLock (HashMap<String, Number> componentCounts){
			updateResults(timeLimit, componentCounts);
		}
	}
	
	/*
	 * This is a less efficient class implementation than the
	 * above for results, however it does not require that we
	 * have a fixed time limit.
	 */
	private class TraceResultsUnlimitedTime implements TraceResults {

		private String[] componentNames;
		private LinkedList<Number>[] results;
		private LinkedList<Number> timepoints;
		private double ourTimeIndex;
		
		int numberOfResults;
		
		public int numberOfAggregatedResults (){
			return this.numberOfResults;
		}
		
		public void aggregateResults (TraceResults extraResults){
			int numberExtraResults = extraResults.numberOfAggregatedResults();
			int totalNumberResults = this.numberOfResults + numberExtraResults;
			
			/* We assume that the component names are the same
			 * and that the time points are the same.
			 */
			for (int nameIndex = 0; nameIndex < results.length; nameIndex++){
				LinkedList<Number> theseResults = results[nameIndex];
				double [] newResults = extraResults.getTimeSeries(nameIndex);
				LinkedList<Number> updatedResults = new LinkedList<Number>();
				for (int timeIndex = 0; 
						timeIndex < Math.min(newResults.length, theseResults.size()); 
							timeIndex++){
					double newValue = ((theseResults.get(timeIndex).doubleValue() * this.numberOfResults) +
					                   (newResults[timeIndex] * numberExtraResults)) / 
					                   totalNumberResults;
					updatedResults.addLast(newValue);
				}
				results[nameIndex] = updatedResults;
			}
			
			
			this.numberOfResults = totalNumberResults;
		}
		
		
		@SuppressWarnings("unchecked")
		TraceResultsUnlimitedTime (String[] compNames, double startTime){
			this.componentNames = compNames;
			this.timepoints = new LinkedList<Number>();
			this.results = (LinkedList<Number>[]) new LinkedList[componentNames.length];
			for (int index = 0; index < componentNames.length; index++){
				results[index] = new LinkedList<Number>();
			}
			this.numberOfResults = 1;
		}
		
		public boolean updateResults (double newTotalTime,
				HashMap<String, Number> componentCounts){
			while (ourTimeIndex <= newTotalTime){
				timepoints.addLast(ourTimeIndex);
				for (int index = 0; index < componentNames.length; index++){
					// TODO: probably should check if this comes back with null
					double thisValue = componentCounts.get(componentNames[index]).doubleValue();
					results[index].addLast(thisValue);
				}
				ourTimeIndex += dataPointStep;
			}
			
			// We always let the simulation continue.
			return true;
		}
		
		public void completeDeadLock (HashMap<String, Number> componentCounts){
			updateResults(timeLimit, componentCounts);
		}
		
		
		public String[] getActionNames() {
			// Throughput not supported
			return null;
		}

		public double getActionThroughput(int index) {
			// Throughput not supported
			return 0;
		}

		public String[] getComponentNames() {
			return componentNames;
		}

		public Map<String, Number> getModelParameters() {
			// Model parameters not supported
			return null;
		}

		public double getPopulation(int index) {
			return results[index].getLast().doubleValue();
		}

		public String getSimulatorName() {
			return "Trace-simulation";
		}

		public Map<String, Number> getSimulatorParameters() {
			// Simulation parameters not supported
			return null;
		}

		private double[] convertLinkedList(LinkedList<Number> list){
			double [] values = new double [list.size()];
			int index = 0;
			// This assumes that the iterator goes in order
			// which I think it does??
			for (Number value : list){
				values[index] = value.doubleValue();
				index++;
			}
			
			return values;
		}
		
		public double[] getTimePoints() {
			return convertLinkedList(timepoints);
		}

		public double[] getTimeSeries(int index) {
			return convertLinkedList(results[index]);
		}

		public boolean throughputSupported() {
			return false;
		}
		
		protected double simulationRunTime;
		public void setSimulationRunTime(double s){
			this.simulationRunTime = s;
		}
		
		public double getSimulationRunTime(){
			return this.simulationRunTime;
		}
		
		/*
		 * Normalise these results to the new set of time points.
		 * Note that this doesn't do anything for throughput values
		 * but clearly should if that is true.
		 */
		public void normaliseResult (double [] newTimePoints){
			/* Note we could avoid making whole new arrays if newTimePoints
			 * is the same length as the old time points array.
			 */
			LinkedList<Number>[] newResults =
				(LinkedList<Number>[]) new LinkedList[componentNames.length];
			for (int index = 0; index < componentNames.length; index++){
				newResults[index] = new LinkedList<Number>();
			}
			
			int oldIndex = 0;
			for (int newIndex = 0; newIndex < newTimePoints.length; newIndex++){
				/*
				 * Skip past as many old results as we need to, to get to the next
				 * new time point. Note that if there are more new time points then
				 * the for-loop will execute many times without executing the while
				 * loop.
				 */
				while (timepoints.get(oldIndex).doubleValue() < newTimePoints[newIndex] && 
						oldIndex < timepoints.size()){
					oldIndex++;
				}
				/*
				 * Once we have the corresponding oldIndex we just update the new results
				 */
				for (int nameIndex = 0; nameIndex < componentNames.length; nameIndex++){
					newResults[nameIndex].add(results[nameIndex].get(oldIndex));
				}
				
			}
			this.results = newResults;
			this.timepoints = new LinkedList<Number>();
			for (int timeIndex = 0; timeIndex < newTimePoints.length; timeIndex++){
				timepoints.add(newTimePoints[timeIndex]);
			}
			
		}

		/*
		 * (non-Javadoc)
		 * @see uk.ac.ed.inf.biopepa.core.interfaces.Result#concatenateResults(uk.ac.ed.inf.biopepa.core.interfaces.Result)
		 * TODO: this is unimplemented but there is no reason why it
		 * cannot be implemented, and it could be useful for doing an
		 * unlimited time simulation with phases.
		 */
		public void concatenateResults(Result result) throws BioPEPAException {
			throw new BioPEPAException ("Concatenation of results unsupported");
		}
		
	}
	
	private double dataPointStep = 1.0;
	public void setDataPointStep (double newDataPointStep){
		this.dataPointStep = newDataPointStep;
	}
	
	private TraceResults simulationResults;
	public Result getSimulationResults(){
		return this.simulationResults;
	}
	private LinkedList<Result> listOfAllResults;
	public LinkedList<Result> getAllSimulationResults(){
		return this.listOfAllResults;
	}
	
	/* Now for phases */
	/* Now we set up the phases */
	// private double[] delays = { 50, 50, 50 };
	private PhaseLine [] phaseLines;
	public void setPhaseLines (PhaseLine[] phaseLines){
		this.phaseLines = phaseLines;
	}
	
	public interface SimulationTraceLog {
		/*
		 * Provide the header to the trace file
		 * so this may be a null operation or it may
		 * set up the head and open body statements of
		 * an html page (for example).
		 */
		public void traceLogHeader (HashMap<String, Number> componentCounts) throws IOException;
		/*
		 * Many of these methods have arguments which are convenient for
		 * the traviando trace output simply because that is the first one
		 * implemented. We may find that these should be updated to accomodate
		 * other formats.
		 */
		public void traceLogFooter (Result result) throws IOException;
		public void displayComponentCounts (HashMap<String, Number> componentCounts) throws IOException;
		public void displayEnabledReaction(String rName, double rValue) throws IOException;
		public void startEvent(String rname, double totalTime) throws BioPEPAException, IOException;
		public void outputComponentUpdate(String rName, int newValue) throws IOException;
		public void endEvent(double thisDelay, double totalRate, HashMap<String, Number> componentCounts) throws IOException;
		public void reportDeadlocked () throws IOException;
	}
	
	/*
	 * Generates many simulation traces using a list of
	 * simulation trace loggers to record the results.
	 * We should be able to combine the results of each simulation
	 * trace, into one average result but currently that is not
	 * implemented
	 */
	public void generateLotsOfTraces (List<SimulationTraceLog> traceLoggers) 
		throws BioPEPAException, IOException{
		this.listOfAllResults = new LinkedList <Result>();
		TraceResults aggregatedResults = null;
		for (SimulationTraceLog traceLog : traceLoggers){
			generateSimulationTrace(traceLog);
			this.listOfAllResults.addLast(this.simulationResults);
			if (aggregatedResults == null){
				aggregatedResults = this.simulationResults;
			} else {
				aggregatedResults.aggregateResults(this.simulationResults);
			}
		}
		this.simulationResults = aggregatedResults;
	}
	
	public class SimulationCompleter {
		private String targetName;
		private Number targetValue;
		private boolean targetMet = false;
		SimulationCompleter (String componentName, Number value){
			this.targetName = componentName;
			this.targetValue = value;
		}

		private double startTime;
		private int timepointsSize;
		private int numberResults;
		private int[] buckets;
		public void initialise (double startTime, double stopTime, int runs){
			// First work out how many time points there will be
			double fakeTime = startTime; 
			while (fakeTime < timeLimit){
				timepointsSize++;
				fakeTime += dataPointStep;
			}
			this.buckets = new int [timepointsSize];
			this.startTime = startTime;
			this.numberResults = runs;
		}
		
		public boolean targetComplete (double currentTime, 
				HashMap<String, Number> componentCounts,
				ExperimentLine currentLine){
			// First get the integer count of the target component
			Number count = componentCounts.get(this.targetName);
			// If this is actually null then the target component must
			// actually be a dynamic variable rather than a component
			// so we "simply" have to evaluate the expression of the
			// dynamic variable with the current component counts.
			if (count == null){
				// We're dealing with a dynamic variable:
				CompiledExpression exp = sbaModel.getDynamicExpression(this.targetName);
				CompiledExpressionEvaluator expVisitor = 
					new CompiledExpressionEvaluator (sbaModel,
							  componentCounts, currentTime);
				exp.accept(expVisitor);
				double expValue = expVisitor.getResult(); 
				count = (int) Math.floor(expValue);
			}
			// int comparison = count.count.compareTo(this.targetValue);	
			if (count.intValue() >= this.targetValue.intValue()){
				double timeSinceStart = Math.max(0, currentTime - startTime);
				int bucket = (int) Math.floor(timeSinceStart / dataPointStep);
				this.buckets[bucket] += 1;
				return true;
			}			
			return false;
		}
		
		public double[] computeTimePoints(){
			double [] timepoints = new double[buckets.length];
			double time = startTime;
			for (int i = 0; i < buckets.length; i++){
				timepoints[i] = time;
				time += dataPointStep;
			}
			return timepoints;
		}
		
		public double[] computeCdf() throws BioPEPAException{
			double[] cdfPoints = new double [buckets.length];
			// double accumulativeProbability = 0.0;
			int completed = 0;
			for (int index = 0; index < cdfPoints.length; index++){
				// Add to the accumulativeProbability the probability
				// that we completed within this bucket.
				completed += buckets[index];
				double accumulativeProbability = (((double)completed) / numberResults);
				// because of rounding errors the value is occasionally over
				// 1.0, this is annoying especially as the graph will then be
				// drawn with a y-axis from 0-2. So we take the minimum
				// but check if the value exceeds 1.1 (arbitrary value above
				// 1 which wouldn't be caused simply by rounding errors.
				cdfPoints[index] = 100 * accumulativeProbability;
				if (accumulativeProbability > 1.001){
					BioPEPAException e = new BioPEPAException ("cdf value above one");
					throw e;
				}
			}
			return cdfPoints;
		}
		/*
		 * This is not actually correct it won't produce a
		 * proper probability density function, in particular it
		 * will never computer a value greater than 1 which is
		 * possible for pdfs.
		 */
		public double[] computePdf(){
			double[] pdfPoints = new double [buckets.length];
			for (int index = 0; index < pdfPoints.length; index++){
				// Add to the accumulativeProbability the probability
				// that we completed within this bucket.
				pdfPoints[index] = 100 * ((double)buckets[index] / numberResults);
			}
			return pdfPoints;
		}
	};
	SimulationCompleter simulationCompleter;
	public void calculateDistribution(String comp, 
			Integer value, int runs, ProgressMonitor pg) 
		throws BioPEPAException, IOException{
		this.simulationCompleter = new SimulationCompleter(comp, value);
		
		pg.beginTask(runs);
		
		simulationCompleter.initialise(0.0, timeLimit, runs);
		for (int i = 0; i < runs; i++){
			generateSimulationTrace (new NullTraceLog());
			pg.worked(1);
		}
		
		pg.done();
	}
	public double[] getDistributionTimePoints(){
		return this.simulationCompleter.computeTimePoints();
	}
	public double[] getDistributionCdf() throws BioPEPAException{
		return this.simulationCompleter.computeCdf();
	}
	public double[] getDistributionPdf(){
		return this.simulationCompleter.computePdf();
	}
	/* Generates a simulation trace into a string consumer.
	 * Note that we do not open and close the string consumer on the
	 * basis that you may wish to write more to it either before or
	 * after. Also any caller can then worry about closing the string
	 * consumer if we do not end successfully and instead throw an exception
	 */
	public void generateSimulationTrace(SimulationTraceLog traceLog) 
		throws BioPEPAException, IOException {
		String[] componentNames = sbaModel.getComponentNames();
		HashMap<String, Number> componentCounts = new HashMap<String, Number>();	
		/* Set up the initial concentrations of each kind of
		 * component.
		 */
		for (ComponentNode cn : sbaModel.getComponents()) {
			Integer componentCount = 0;
			// CompartmentData cd = cn.getCompartment();
			String componentName = cn.getName();
			/*
			 * This code to be used with an experiment line. Number
			 * componentCountNumber = componentOverrides.get(componentName)
			 * ; double componentCount ; if (componentCountNumber == null) {
			 * componentCount = cn.getCount () ; } else { componentCount =
			 * componentCountNumber.doubleValue(); }
			 */
			// Okay we have nothing for compartments
			componentCount = new Integer((int) cn.getCount());
			componentCounts.put(componentName, componentCount);
		}

		traceLog.traceLogHeader(componentCounts);

		
		// ExperimentSet experimentSet = new ExperimentSet();
		// ExperimentLine experimentLine = experimentSet.emptyExperimentLine("traviando-export");
		SBAReaction[] sbaReactions = sbaModel.getReactions();
		
		
		
		
		// We require a new results object to record the results
		TraceResults results;
		if (timeLimit == Double.MAX_VALUE){
			results = new TraceResultsUnlimitedTime (componentNames, 0.0);
		} else {
			results = new TraceResultsFixedTime(componentNames, 0.0);
		}
		
		// This number should clearly be passed in as an option
		// or we should pass in a time at which the simulation
		// should stop.
		double totalTime = 0.0;
		int numberOfFirings = this.numberFiringsLimit;
		
		/* Finally before we start initialise the phase criteria */
		int currentPhase = 0;
		// If the phase lines have not been set we assume that we
		// are not interested in phases and set up a dummy one to
		// last the entire duration of the simulation.
		if (phaseLines == null){
			ExperimentLine zeroPhase = new ExperimentLine("zero-phase");
			phaseLines = new PhaseLine[1];
			phaseLines[0] = new PhaseLine(zeroPhase, timeLimit);
		}
		PhaseLine currentPhaseLine = phaseLines[currentPhase];
		double currentPhaseDelay = currentPhaseLine.getDuration();
		ExperimentLine currentLine = currentPhaseLine.getExperimentLine();
		while (numberOfFirings-- >= 0 && totalTime < timeLimit) {
			double totalRate = 0.0;
			
			traceLog.displayComponentCounts(componentCounts);

			double [] reactionRates = new double[sbaReactions.length];
			/*
			 * TODO: we need to figure out if a reaction is indeed enabled,
			 * in particular if it has inhibitors with non-zero populations
			 * or catylsts with zero populations then I *think* the reaction
			 * should not be enabled.
			 */
			for (int index = 0; index < sbaReactions.length; index++){
				SBAReaction reaction = sbaReactions[index];
				CompiledExpressionRateEvaluator rateEval =
					new CompiledExpressionRateEvaluator(sbaModel, 
						componentCounts, totalTime, reaction);
				reaction.getRate().accept(rateEval);
				double rateValue = rateEval.getResult();
				reactionRates[index] = rateValue;
				
				traceLog.displayEnabledReaction(reaction.getName(), rateValue);
				totalRate += rateValue;
			}

			// Given the total rate we can now decide on a total delay.
			double thisDelay = expDelay(1 / totalRate);
			
			// First we check if the delay is Infinity or more rather
			// the rate is zero, if this is the case then we do not
			// wish to complete however rather than check this,
			if (totalRate <= 0){
				
				traceLog.reportDeadlocked();
				results.completeDeadLock(componentCounts);
				break;
			}
			
			// Now given that delay we now decide if in stead of performing
			// some reaction we should actually perform a phase shift.
			// We do this before we choose a reaction since there is no
			// need to choose the reaction if we are shifting phase.
			if (thisDelay > currentPhaseDelay){
				// First up date the total time to be that of the remainder
				// of the phase delay, ie. taking us up to the phase shift
				totalTime += currentPhaseDelay;
				// Now switch phases
				// Could this be better written currentPhase = (currentPhase + 1) % delays.length ??
				currentPhase++;
				if (currentPhase >= phaseLines.length){
					currentPhase = 0;
				}
				// Now update the currentPhase delay to be the whole
				// of the delay of the new phase
				currentPhaseLine = phaseLines[currentPhase];
				currentPhaseDelay = currentPhaseLine.getDuration();
				// And of course update the current line to the new phase
				currentLine = currentPhaseLine.getExperimentLine();
				
				// Isn't really required, but doesn't do any harm either.
				results.updateResults(totalTime, componentCounts);
				// System.out.println("currentPhase = " + currentPhase + ", thisDelay = "+ thisDelay + ", totalTime = " + totalTime);
				continue;
			} else {
				// If we are not switching phases then update the time
				// based on the chosen delay and also update the remaining
				// delay of this phase by subtracting from it the time taken
				// for this reaction to occur (ie. this delay).
				totalTime += thisDelay;
				currentPhaseDelay -= thisDelay;
			}
			
			
			double passedProbability = 0;
			double picker = generator.nextDouble();
			double chooser = totalRate * picker;
			SBAReaction chosen = null;
			for (int index = 0; index < sbaReactions.length; index++){
				SBAReaction reaction = sbaReactions[index];
				// Rather than re-generating the rate here each
				// time we should probably just go ahead and do
				// that prior to running the simulation a la
				// mapModel.
				// Value rValue = isbjava.generateRate(reaction, experimentLine, reaction.isReversible());
				// double rateValue = 1.0; // rValue.getValue(symbolEvaluator);				
				// RateVisitor rateVisitor = new RateVisitor(totalTime, componentCounts, reaction);
				// reaction.getRate().accept(rateVisitor);
				double rateValue = reactionRates[index];
				passedProbability += rateValue;
				if (chooser < passedProbability) {
					chosen = reaction;
					break;
				}
			}
			
			/*
			 * We may get here without being able to choose a reaction
			 * if we aren't using phases, otherwise this situation should
			 * be caught above.
			 */
			if (chosen == null) {
				traceLog.reportDeadlocked();
				results.completeDeadLock(componentCounts);
				break;
			}

			String rname = chosen.getName();
			traceLog.startEvent(rname, totalTime);
			

			// At this point we should update the results:
			// Note that we do this before we update the component counts
			// since the current component counts will remain until the END
			// of this delay.
			results.updateResults(totalTime, componentCounts);
			
			
			// Now we must update the component populations based
			// on the chosen reaction

			// First the reactants
			
			for (SBAComponentBehaviour cb : chosen.getReactants()) {
				if (cb.getType().equals(Type.REACTANT)) {
					String rName = cb.getName();
					Number current = componentCounts.get(rName);
					if (current == null) {
						throw new BioPEPAException("reactant (" + rName + ") not in map");
					}
					int newValue = current.intValue() - cb.getStoichiometry();
					componentCounts.put(rName, newValue);
					traceLog.outputComponentUpdate(rName, newValue);
				}
			}
			

			// Second the products
			
			for (SBAComponentBehaviour cb : chosen.getProducts()) {
				String pName = cb.getName();
				Number current = componentCounts.get(pName);
				if (current == null) {
					throw new BioPEPAException("product (" + pName + ") not in map");
				}
				int newValue = current.intValue() + cb.getStoichiometry();
				componentCounts.put(pName, newValue);
				traceLog.outputComponentUpdate(pName, newValue);
			}
			
			traceLog.endEvent(thisDelay, totalRate, componentCounts);
			
			if (this.simulationCompleter != null &&
					this.simulationCompleter.targetComplete(totalTime, 
														componentCounts, currentLine)){
				break;
			}
		} // End of the while loop running the simulation.

		traceLog.traceLogFooter(results);
		this.simulationResults = results;
	}
	
	public static class NullTraceLog implements SimulationTraceLog {

		public void displayComponentCounts(HashMap<String, Number> componentCounts) throws IOException {
			return;
		}

		public void displayEnabledReaction(String rName, double rValue) throws IOException {
			return;
		}

		public void endEvent(double thisDelay, double totalRate, 
				HashMap<String, Number> componentCounts)
				throws IOException {
			return;
		}

		public void outputComponentUpdate(String rName, int newValue) throws IOException {
			return ;
		}

		public void reportDeadlocked() throws IOException {
			return ;
		}

		public void startEvent(String rname, double totalTime) throws BioPEPAException, IOException {
			return ;
		}

		public void traceLogFooter(Result _result) throws IOException {
			return ;
		}

		public void traceLogHeader(HashMap<String, Number> componentCounts) throws IOException {
			return ;
		}
		
	}
}
