package uk.ac.ed.inf.biopepa.core;

import java.util.*;

import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.sba.Parameters;
import uk.ac.ed.inf.biopepa.core.sba.Parameters.Parameter;

public class BasicResult implements Result {
	protected String simulator = "unknown";
	protected String[] actionNames, componentNames;

	protected Map<String, Number> modelParameters = 
		new HashMap<String, Number>(), uModelParameters = Collections
			.unmodifiableMap(modelParameters);

	protected Map<String, Number> simulatorParameters = 
		new HashMap<String, Number>(), uSimulatorParameters = Collections
			.unmodifiableMap(simulatorParameters);

	protected boolean throughput = false;
	protected double[] throughputValues;
	protected double [] timePoints;
	protected double[][] results;
	protected double simulationRunTime;
	
	public BasicResult(Parameters parameters, Map<String, Number> modelParameters) {
		for (Map.Entry<Parameter, Object> me : parameters.getParameters().entrySet())
			if (!me.getKey().equals(Parameter.Components) && me.getValue() instanceof Number)
				simulatorParameters.put(me.getKey().getDescriptiveName(), (Number) me.getValue());
		for (Map.Entry<String, Number> me : modelParameters.entrySet())
			this.modelParameters.put(me.getKey(), me.getValue());
	}
	
	public BasicResult(){
	}
	
	public void setSimulator(String sim){
		this.simulator = sim;
	}
	public void setTimePoints(double [] times){
		this.timePoints = times;
	}
	
	public void setResults(double[][] results){
		this.results = results;
	}

	public String[] getActionNames() {
		if (actionNames == null)
			return new String[] {};
		return actionNames.clone();
	}

	public double getActionThroughput(int index) {
		return throughputValues[index];
	}

	public void setComponentNames (String[] names){
		this.componentNames = names;
	}
	public String[] getComponentNames() {
		if (componentNames == null)
			return new String[] {};
		return componentNames.clone();
	}

	public Map<String, Number> getModelParameters() {
		return uModelParameters;
	}

	public double getPopulation(int index) {
		return results[index][results[0].length - 1];
	}

	public String getSimulatorName() {
		return simulator;
	}

	public Map<String, Number> getSimulatorParameters() {
		return uSimulatorParameters;
	}

	public double[] getTimeSeries(int index) {
		return results[index];
	}

	public boolean throughputSupported() {
		return throughput;
	}

	public double[] getTimePoints() {
		return timePoints;
	}
	
	public void setSimulationRunTime(double s){
		this.simulationRunTime = s;
	}
	
	public double getSimulationRunTime(){
		return this.simulationRunTime;
	}
	
	/*
	 * Normalise these results to the new set of time points.
	 */
	public void normaliseResult (double [] newTimePoints){
		/* Note we could avoid making whole new arrays if newTimePoints
		 * is the same length as the old time points array.
		 */
		double[][] newResults = new double [componentNames.length][];
		for (int index = 0; index < componentNames.length; index++){
			newResults[index] = new double [newTimePoints.length];
		}
		
		double [] newThroughput = null;
		if (this.throughput){
			newThroughput = new double [newTimePoints.length];
		} 
		
		int oldIndex = 0;
		for (int newIndex = 0; newIndex < newTimePoints.length; newIndex++){
			/*
			 * Skip past as many old results as we need to, to get to the next
			 * new time point. Note that if there are more new time points then
			 * the for-loop will execute many times without executing the while
			 * loop.
			 */
			while (timePoints[oldIndex] < newTimePoints[newIndex] && 
					oldIndex < timePoints.length){
				oldIndex++;
			}
			/*
			 * Once we have the corresponding oldIndex we just update the new results
			 */
			for (int nameIndex = 0; nameIndex < componentNames.length; nameIndex++){
				newResults[nameIndex][newIndex] = results[nameIndex][oldIndex];
			}
			/*
			 * Now if throughput is supported update the throughput value.
			 */
			if (this.throughput){
				newThroughput[newIndex] = throughputValues[oldIndex];
			}
			
		}
		this.results = newResults;
		this.timePoints = newTimePoints;
		if (this.throughput){
			this.throughputValues = newThroughput;
		}
	}

	/*
	 * Concatenate two double arrays, the given offset is how much of the
	 * second one to ignore.
	 */
	private double[] concatenateDoubleArrays(double []a1, double[]a2, int offset){
		double [] result = new double [a1.length + a2.length - offset];
		for (int index = 0; index < a1.length; index++){
			result[index] = a1[index];
		}
		for (int index = offset; index < a2.length; index++){
			result[index + a1.length - offset] = a2[index];
		}
		
		return result;
	}
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ed.inf.biopepa.core.interfaces.Result#concatenateResults(uk.ac.ed.inf.biopepa.core.interfaces.Result)
	 * It's a little tempting to more aggressively check the constraints
	 * which we put on the incoming results. That is that they should have
	 * the same names plotted and should start from time point 0.0
	 */
	public void concatenateResults(Result result) {
		double [] extraTimePoints = result.getTimePoints();
		double [] additionalTimePoints = new double [extraTimePoints.length];
		double lastCurrent = this.timePoints[this.timePoints.length - 1];
		// First of all, add
		for (int index = 0; index < additionalTimePoints.length; index++){
			additionalTimePoints[index] = lastCurrent + extraTimePoints[index];
		}
		// Now when we add the time points together we do not
		// wish to include the first timepoint as that should
		// be 0.0
		double [] newTimePoints = 
			concatenateDoubleArrays(this.timePoints, additionalTimePoints, 1);
		this.timePoints = newTimePoints;
		
		double[][] newResults = new double[this.results.length][];
		for (int index = 0; index < newResults.length; index++){
			newResults[index] = 
				concatenateDoubleArrays(this.results[index], 
						result.getTimeSeries(index), 1);
		}
		this.results = newResults;
		
		/*
		 * Throughput is supposed to be the throughput at the end
		 * of the simulation hence we just wish to make the throughput
		 * here equal to that of the given result.
		 */
		if (this.throughput && result.throughputSupported()){
			for (int index = 0; index < this.throughputValues.length; index++){
				this.throughputValues[index] = result.getActionThroughput(index);
			}
		}
				
		/*
		 * Finally the simulation time should be equal to the addition
		 * of both results' simulation times
		 */
		this.simulationRunTime += result.getSimulationRunTime();
		
	}
}
