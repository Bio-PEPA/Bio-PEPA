package uk.ac.ed.inf.biopepa.core.sba;

import java.util.HashMap;
import java.util.Map.Entry;

import uk.ac.ed.inf.biopepa.core.BasicResult;
import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpression;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpressionEvaluator;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledNumber;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledOperatorNode;
import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledOperatorNode.Operator;
import uk.ac.ed.inf.biopepa.core.interfaces.ProgressMonitor;
import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.interfaces.Solver;
import uk.ac.ed.inf.biopepa.core.sba.Parameters.Parameter;

public class NativeRungaKutta implements Solver {

	public String getDescriptiveName() {
		return "A native implementation of Rutta Kunga";
	}
	
	public String getShortName() {
		return "native-rk";
	}

	public Parameters getRequiredParameters() {
		Parameters parameters = new Parameters();
		parameters.add(Parameter.Start_Time);
		parameters.add(Parameter.Stop_Time);
		parameters.add(Parameter.Data_Points);
		parameters.add(Parameter.Components);
		parameters.add(Parameter.Step_Size);
		parameters.add(Parameter.Absolute_Error);
		parameters.add(Parameter.Relative_Error);
		return parameters;
	}

	public SolverResponse getResponse(final SBAModel model) {
		return new SolverResponse() {
			public String getMessage() {
				if (model.nonDifferentiableFunctions)
					return "The model uses functions that may invalidate the results from ODE analysis.";
				return null;
			}

			public Suitability getSuitability() {
				if (model.nonDifferentiableFunctions)
					return Suitability.WARNING;
				return Suitability.PERMISSIBLE;
			}
		};
	}

	
	private CompiledExpression odeAdd (CompiledExpression left,
			CompiledExpression bareRight, int affect){
		if (affect == 0){
			return left; // even if it is null
		}
		CompiledExpression right;
		if (affect == 1){
			right = bareRight;
		} else {
			CompiledOperatorNode mult = new CompiledOperatorNode ();
			mult.setOperator(Operator.MULTIPLY);
			mult.setLeft(new CompiledNumber (new Integer (affect)));
			mult.setRight(bareRight);
			right = mult;
		}
		if (left == null){
			return right;
		} else {
			CompiledOperatorNode addition = new CompiledOperatorNode();
			addition.setOperator(Operator.PLUS);
			addition.setLeft(left);
			addition.setRight(right);
			return addition;
		}
	}

	private double[] createTimePoints (double startTime, double stopTime, Integer dataPoints){
		double[] timepoints = new double [dataPoints];
		double fakeTime = startTime;
		double timeStep = (stopTime - startTime) / dataPoints.doubleValue(); 
		for (int timeIndex = 0; timeIndex < dataPoints; timeIndex++){
			timepoints[timeIndex] = fakeTime;
			fakeTime += timeStep;
		}
		return timepoints;
	}
	
	public Result startTimeSeriesAnalysis(SBAModel sbaModel,
			Parameters parameters, ProgressMonitor monitor)
			throws BioPEPAException {
		BasicResult result= new BasicResult ();
		
		Integer dataPoints = (Integer) parameters.getValue(Parameter.Data_Points);
		double startTime = (Double) parameters.getValue(Parameter.Start_Time);
		double stopTime = (Double) parameters.getValue(Parameter.Stop_Time);
		double stepSize = (Double) parameters.getValue(Parameter.Step_Size);
		
		double[] timepoints = createTimePoints(startTime, stopTime, dataPoints);
		String[] componentNames = (String[]) parameters.getValue(Parameter.Components);
		
		double[][] results = new double[componentNames.length][];
		for (int index = 0; index < results.length; index++){
			results[index] = new double[timepoints.length];
		}
		
		
		result.setTimePoints(timepoints);
		result.setResults(results);
		result.setComponentNames(componentNames);
		
		HashMap<String, Number> componentCounts = new HashMap<String, Number>();
		
		ComponentNode[] componentNodes = sbaModel.getComponents();
		for (ComponentNode cn : componentNodes) {
			Double componentCount = 0.0;
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
			componentCount = new Double ((double) cn.getCount());
			componentCounts.put(componentName, componentCount);
		}
		
		/*
		 * Now we actually perform the algorithm.
		 */
		double simulationTime = startTime;
		int timeIndex = 0;
		SBAReaction[] reactions = sbaModel.getReactions();
		
		// We need a compiled expression for every component in
		// the model not just those in the results.
		CompiledExpression [] odes = new CompiledExpression[componentNodes.length];
		for (int odeIndex = 0; odeIndex < odes.length; odeIndex++){
			String componentName = componentNodes[odeIndex].getName();
			CompiledExpression ode = null;
			for (SBAReaction reaction : reactions){
				int affect = reaction.netAffect(componentName);
				ode = odeAdd(ode, reaction.getRate(), affect);
			}
			odes[odeIndex] = ode;
		}
		
		monitor.beginTask(timepoints.length);
		while (simulationTime <= stopTime && timeIndex < timepoints.length){
			// So in particular 0.0 is <= 0.0
			// Also note that this assumes that the stepTime is smaller than
			// the differences between two time points in the results.
			// Because this will only fill in at most one time point per
			// iteration, so if the step size was 0.1 but we asked for 1000
			// data points from 0 - 10, then we would be in trouble.
			if (timepoints[timeIndex] <= simulationTime){
				for (int compIndex = 0; compIndex < componentNames.length; compIndex++){
					Number countNum = componentCounts.get(componentNames[compIndex]);
					double count = countNum.doubleValue();
					results[compIndex][timeIndex] = count;
				}
				monitor.worked(1);
				timeIndex++;
			}
			
			// Record the current values of the components not only
			// for quick retrieval but because we will be modifying
			// componentCounts in order to compute the intermeidate
			// values. 
			double[] k0values = new double[componentNodes.length];
			for (int index = 0; index < componentNodes.length; index++){
				String compName = componentNodes[index].getName();
				double currentValue = componentCounts.get(compName).doubleValue();
				k0values[index] = currentValue;
			}
			
			double [] k1values = new double[componentNodes.length];
			for (int index = 0; index < componentNodes.length; index++){
				CompiledExpression changeExp = odes[index];
				if (changeExp != null){
					CompiledExpressionEvaluator eval = 
						new CompiledExpressionEvaluator(sbaModel,
								componentCounts, simulationTime);
					changeExp.accept(eval);
					double value = eval.getResult();
					// k1 = h * fx(t0, y0);
					k1values[index] = stepSize * value;
				} else {
					k1values[index] = 0;
				}
			}
			// Update component counts so that we can use it to evaluate
			// the odes in computation of the k2 values.
			// Now we are computing k2=h*fx(x0+h/2,y0+k1/2);
			// So the value to which each component is set is the current
			// value plus a half of the change for k1
			for (int index = 0; index < componentNodes.length; index++){
				String compName = componentNodes[index].getName();
				double currentValue = k0values[index];
				double change = k1values[index];
				// The y0+k1/2 portion above.
				componentCounts.put(compName, currentValue + (change / 2));
			}
			// We can now compute the k2 values.
			double [] k2values = new double[componentNodes.length];
			for (int index = 0; index < componentNodes.length; index++){
				CompiledExpression changeExp = odes[index];
				if (changeExp != null){
					CompiledExpressionEvaluator eval = 
						new CompiledExpressionEvaluator(sbaModel,
								componentCounts, simulationTime + stepSize/2);
					changeExp.accept(eval);
					double value = eval.getResult();
					k2values[index] = stepSize * value;
				} else {
					k2values[index] = 0;
				}
			}
			
			// Similarly we must now update the component counts so as
			// to evaluate the odes in order to compute k3
			// We are now wishing to compute:
			// k3=h*fx(x0+h/2,y0+k2/2)
			for (int index = 0; index < componentNodes.length; index++){
				String compName = componentNodes[index].getName();
				double currentValue = k0values[index];
				double change = k2values[index];
				// The y0+k2/2 portion above.
				componentCounts.put(compName, currentValue + (change / 2));
			}
			// We can now compute the k3 values.
			double [] k3values = new double[componentNodes.length];
			for (int index = 0; index < componentNodes.length; index++){
				CompiledExpression changeExp = odes[index];
				if (changeExp != null){
					CompiledExpressionEvaluator eval = 
						new CompiledExpressionEvaluator(sbaModel,
								componentCounts, simulationTime + stepSize/2);
					changeExp.accept(eval);
					double value = eval.getResult();
					k3values[index] = stepSize * value;
				} else {
					k3values[index] = 0;
				}
			}
			
			// Finally we wish to compute k4 = h*fx(x0+h,y0+k3);
			// Again we compute the new componentCounts environment
			for (int index = 0; index < componentNodes.length; index++){
				String compName = componentNodes[index].getName();
				double currentValue = k0values[index];
				double change = k3values[index];
				// The y0+k2/2 portion above.
				componentCounts.put(compName, currentValue + change);
			}
			// We can now compute the k4 values.
			double [] k4values = new double[componentNodes.length];
			for (int index = 0; index < componentNodes.length; index++){
				CompiledExpression changeExp = odes[index];
				if (changeExp != null){
					CompiledExpressionEvaluator eval = 
						new CompiledExpressionEvaluator(sbaModel,
								componentCounts, simulationTime + stepSize);
					changeExp.accept(eval);
					double value = eval.getResult();
					k4values[index] = stepSize * value;
				} else {
					k4values[index] = 0;
				}
			}
			
			/*
			 * So the new current values are the old values
			 * plus (1/6)h(k1 + k2 + k2 + k3 +k3 + k4)
			 */
			for (int index = 0; index < componentNodes.length; index++){
				String compName = componentNodes[index].getName();
				double currentValue = k0values[index];
				double k1 = k1values[index];
				double k2 = k2values[index];
				double k3 = k3values[index];
				double k4 = k4values[index];
				double change = (1.0/6.0) * (k1 + k2 + k2 + k3 + k3 + k4);
				double newValue = currentValue + change;
				componentCounts.put(compName, newValue);
			}
			
			
			// Update the simulation time.
			simulationTime += stepSize;
		}
		 
		
		return result;
	}

}
