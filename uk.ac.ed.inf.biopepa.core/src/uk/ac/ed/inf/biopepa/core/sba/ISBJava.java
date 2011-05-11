/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.sba;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.systemsbiology.chem.Compartment;
import org.systemsbiology.chem.Model;
import org.systemsbiology.chem.Reaction;
import org.systemsbiology.chem.ReservedSymbolMapperChemCommandLanguage;
import org.systemsbiology.chem.SimulationController;
import org.systemsbiology.chem.SimulationProgressReporter;
import org.systemsbiology.chem.SimulationResults;
import org.systemsbiology.chem.Simulator;
import org.systemsbiology.chem.SimulatorParameters;
import org.systemsbiology.chem.SimulatorStochasticBase;
import org.systemsbiology.chem.SimulatorStochasticGibsonBruck;
import org.systemsbiology.chem.SimulatorStochasticGillespie;
import org.systemsbiology.chem.SimulatorStochasticTauLeapBase;
import org.systemsbiology.chem.SimulatorStochasticTauLeapComplex;
import org.systemsbiology.chem.Species;
import org.systemsbiology.chem.odetojava.SimulatorOdeToJavaBase;
import org.systemsbiology.chem.odetojava.SimulatorOdeToJavaRungeKuttaAdaptive;
import org.systemsbiology.chem.odetojava.SimulatorOdeToJavaRungeKuttaImplicit;
import org.systemsbiology.math.Expression;
import org.systemsbiology.math.Expression.Element;
import org.systemsbiology.math.Expression.ElementCode;
import org.systemsbiology.math.Symbol;
import org.systemsbiology.math.Value;

import uk.ac.ed.inf.biopepa.core.BasicResult;
import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.compiler.CompartmentData;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledDynamicComponent;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpression;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpressionVisitor;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledFunction;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledNumber;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledOperatorNode;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledSystemVariable;
import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;
import uk.ac.ed.inf.biopepa.core.interfaces.ProgressMonitor;
import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.interfaces.Solver;
import uk.ac.ed.inf.biopepa.core.sba.Parameters.Parameter;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour.Type;

public class ISBJava {

	static class DormandPrince implements Solver {

		public String getDescriptiveName() {
			return "Adaptive step-size 5th-order Dormand Prince ODE Solver";
		}

		public Parameters getRequiredParameters() {
			return odeParameters();
		}

		public String getShortName() {
			return "dopr-adaptive";
		}

		public Result startTimeSeriesAnalysis(SBAModel model, Parameters parameters,
				ProgressMonitor monitor) throws BioPEPAException {
			ISBJava isbjava = new ISBJava(model, (String[]) parameters.getValue(Parameter.Components));
			isbjava.simulator = new SimulatorOdeToJavaRungeKuttaAdaptive();
			isbjava.simulatorParameters = ((SimulatorOdeToJavaBase) isbjava.simulator).getDefaultSimulatorParameters();
			isbjava.mapModel();
			try {
				((SimulatorOdeToJavaRungeKuttaAdaptive) isbjava.simulator).initialize(isbjava.model);
			} catch (Exception e) {
				throw new BioPEPAException(e);
			}
			SimulationResults results = isbjava.run(getRequiredParameters(), parameters, monitor);
			if (monitor.isCanceled())
				return null;
			return new Results(results, false, parameters, isbjava.parameterMap, getDescriptiveName());
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
	}

	static class GibsonBruck implements Solver {

		public String getDescriptiveName() {
			return "Gibson-Bruck Stochastic Algorithm";
		}

		public Parameters getRequiredParameters() {
			return stochasticParameters();
		}

		public String getShortName() {
			return "gibson-bruck";
		}

		public Result startTimeSeriesAnalysis(SBAModel model, Parameters parameters,
				ProgressMonitor monitor) throws BioPEPAException {
			ISBJava isbjava = new ISBJava(model, (String[]) parameters.getValue(Parameter.Components));
			isbjava.simulator = new SimulatorStochasticGibsonBruck();
			isbjava.simulatorParameters = ((SimulatorStochasticBase) isbjava.simulator).getDefaultSimulatorParameters();
			isbjava.mapModel();
			try {
				((SimulatorStochasticGibsonBruck) isbjava.simulator).initialize(isbjava.model);
			} catch (Exception e) {
				throw new BioPEPAException(e);
			}
			SimulationResults results = isbjava.run(getRequiredParameters(), parameters, monitor);
			if (monitor.isCanceled())
				return null;
			return new Results(results, true, parameters, isbjava.parameterMap, getDescriptiveName());
		}

		public SolverResponse getResponse(final SBAModel model) {
			return new SolverResponse() {
				public String getMessage() {
					if (model.timeDependentRates)
						return "This algorithm may not correctly simulate models dependent on time.";
					return null;
				}

				public Suitability getSuitability() {
					if (model.timeDependentRates)
						return Suitability.WARNING;
					return Suitability.PERMISSIBLE;
				}
			};
		}
	}

	static class Gillespie implements Solver {

		public String getDescriptiveName() {
			return "Gillespie's Stochastic Algorithm";
		}

		public Parameters getRequiredParameters() {
			return stochasticParameters();
		}

		public String getShortName() {
			return "gillespie";
		}

		public Result startTimeSeriesAnalysis(SBAModel model, Parameters parameters,
				ProgressMonitor monitor) throws BioPEPAException {
			ISBJava isbjava = new ISBJava(model, (String[]) parameters.getValue(Parameter.Components));
			isbjava.simulator = new SimulatorStochasticGillespie();
			isbjava.simulatorParameters = ((SimulatorStochasticBase) isbjava.simulator).getDefaultSimulatorParameters();
			isbjava.mapModel();
			try {
				((SimulatorStochasticGillespie) isbjava.simulator).initialize(isbjava.model);
			} catch (Exception e) {
				throw new BioPEPAException(e);
			}
			SimulationResults results = isbjava.run(getRequiredParameters(), parameters, monitor);
			if (monitor.isCanceled())
				return null;
			return new Results(results, true, parameters, isbjava.parameterMap, getDescriptiveName());
		}

		public SolverResponse getResponse(final SBAModel model) {
			return new SolverResponse() {
				public String getMessage() {
					if (model.timeDependentRates)
						return "This algorithm may not correctly simulate models dependent on time.";
					return null;
				}

				public Suitability getSuitability() {
					if (model.timeDependentRates)
						return Suitability.WARNING;
					return Suitability.PERMISSIBLE;
				}
			};
		}
	}

	static class IMEX implements Solver {

		public String getDescriptiveName() {
			return "Implicit-Explicit Runge Kutta ODE Solver";
		}

		public Parameters getRequiredParameters() {
			return odeParameters();
		}

		public String getShortName() {
			return "imex-stiff";
		}

		public Result startTimeSeriesAnalysis(SBAModel model, Parameters parameters,
				ProgressMonitor monitor) throws BioPEPAException {
			ISBJava isbjava = new ISBJava(model, (String[]) parameters.getValue(Parameter.Components));
			isbjava.simulator = new SimulatorOdeToJavaRungeKuttaImplicit();
			isbjava.simulatorParameters = ((SimulatorOdeToJavaBase) isbjava.simulator).getDefaultSimulatorParameters();
			isbjava.mapModel();
			try {
				((SimulatorOdeToJavaRungeKuttaImplicit) isbjava.simulator).initialize(isbjava.model);
			} catch (Exception e) {
				throw new BioPEPAException(e);
			}
			SimulationResults results = isbjava.run(getRequiredParameters(), parameters, monitor);
			if (monitor.isCanceled())
				return null;
			return new Results(results, false, parameters, isbjava.parameterMap, getDescriptiveName());
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
	}

	static class TauLeap implements Solver {

		public String getDescriptiveName() {
			return "Gillespie's Tau-Leap Stochastic Algorithm";
		}

		public Parameters getRequiredParameters() {
			Parameters p = stochasticParameters();
			p.add(Parameter.Step_Size);
			p.add(Parameter.Relative_Error);
			return p;
		}

		public String getShortName() {
			return "tau-leap";
		}

		public Result startTimeSeriesAnalysis(SBAModel model, Parameters parameters,
				ProgressMonitor monitor) throws BioPEPAException {
			ISBJava isbjava = new ISBJava(model, (String[]) parameters.getValue(Parameter.Components));
			isbjava.simulator = new SimulatorStochasticTauLeapComplex();
			isbjava.simulatorParameters = ((SimulatorStochasticTauLeapBase) isbjava.simulator)
					.getDefaultSimulatorParameters();
			isbjava.mapModel();
			try {
				((SimulatorStochasticTauLeapComplex) isbjava.simulator).initialize(isbjava.model);
			} catch (Exception e) {
				throw new BioPEPAException(e);
			}
			
			SimulationResults results = isbjava.run(getRequiredParameters(), parameters, monitor);
			if (monitor.isCanceled())
				return null;
			Results ourResults = new Results(results, false, parameters, isbjava.parameterMap, getDescriptiveName());
			return ourResults;
		}

		public SolverResponse getResponse(final SBAModel model) {
			return new SolverResponse() {
				public String getMessage() {
					if (model.timeDependentRates)
						return "This algorithm cannot simulate models dependent on time.";
					return null;
				}

				public Suitability getSuitability() {
					if (model.timeDependentRates)
						return Suitability.UNSUITABLE;
					return Suitability.PERMISSIBLE;
				}
			};
		}
	}

	public class RatesVisitor extends CompiledExpressionVisitor {

		Element element;
		SBAReaction reaction;
		// boolean root = true;
		boolean reversing = false;

		public RatesVisitor() {
			super();
		}

		public boolean visit(CompiledDynamicComponent component) {
			// As it's already a reference to a variable of some sort there
			// is nothing to inline or to not inline.
			String name = component.getName();
			CompiledExpression ce = sbaModel.getDynamicExpression(name);
			if (ce != null && !addedParameters.contains(name)) {
				addedParameters.add(name);
				ce.accept(this);
				if (element.mCode.equals(ElementCode.NUMBER))
					model.addParameter(new org.systemsbiology.chem.Parameter(name, element.mNumericValue));
				else
					model.addParameter(new org.systemsbiology.chem.Parameter(name, new Expression(element)));
			}
			
			
			
			element = new Element(ElementCode.SYMBOL);
			element.mSymbol = new Symbol(name);
			return true;
		}

		public boolean visit(CompiledFunction function) {
			if (removeInline(function)){
				return true;
			}
			// root = false;
			// We no longer check if this expression is the root of the
			// expression, since we are allowing built-in functions to occur
			// anywhere in the expression. Hence you may have: fMA(r) + r
			// for example.
			if (function.getFunction().isRateLaw()) {
				Element e1, e2;
				switch (function.getFunction()) {
				case fMA:
					function.getArguments().get(0).accept(this);
					if (element.mCode.equals(ElementCode.NUMBER))
						break; // ISBJava will handle the mass action rate.
					List<SBAComponentBehaviour> reactants = reaction.reactants;
					if (reversing)
						reactants = reaction.products;
					if (reactants.size() == 0)
						break; // constant rate production.
					e1 = prep(reactants.get(0));
					for (int i = 1; i < reactants.size(); i++) {
						e2 = new Element(ElementCode.MULT);
						e2.mSecondOperand = e1;
						e2.mFirstOperand = prep(reactants.get(i));
						e1 = e2;
					}
					e2 = new Element(ElementCode.MULT);
					e2.mSecondOperand = e1;
					e2.mFirstOperand = element;
					element = e2;
					break;
				case fMM:
					function.getArguments().get(0).accept(this);
					e1 = new Element(ElementCode.MULT);
					e1.mFirstOperand = element;
					e2 = new Element(ElementCode.MULT);
					SBAComponentBehaviour substrate = null;
					Element[] eArray = new Element[2];
					int i = 0;
					for (SBAComponentBehaviour cb : reaction.reactants) {
						if (cb.type.equals(Type.REACTANT))
							substrate = cb;
						eArray[i++] = prep(cb);
					}
					e2.mFirstOperand = eArray[0];
					e2.mSecondOperand = eArray[1];
					e1.mSecondOperand = e2;
					e2 = new Element(ElementCode.DIV);
					e2.mFirstOperand = e1;
					e1 = e2;
					e2 = new Element(ElementCode.ADD);
					function.getArguments().get(1).accept(this);
					e2.mFirstOperand = element;
					e2.mSecondOperand = prep(substrate);
					e1.mSecondOperand = e2;
					element = e1;
					break;
				default:
					throw new IllegalStateException();
				}
				return true;
			}
			if (function.getFunction().args() == 1) {
				function.getArguments().get(0).accept(this);
				Element newElement;
				switch (function.getFunction()) {
				case LOG:
					newElement = new Element(ElementCode.LN);
					break;
				case EXP:
					newElement = new Element(ElementCode.EXP);
					break;
				case H:
					newElement = new Element(ElementCode.THETA);
					break;
				case FLOOR:
					newElement = new Element(ElementCode.FLOOR);
					break;
				case CEILING:
					newElement = new Element(ElementCode.CEIL);
					break;
				case TANH:
					newElement = new Element(ElementCode.TANH);
					break;
				default:
					throw new IllegalStateException();
				}
				newElement.mFirstOperand = element;
				element = newElement;
			}
			return true;
		}

		public boolean visit(CompiledNumber number) {
			if (removeInline(number))
				return true;
			element = new Element(number.doubleValue());
			return false;
		}

		public boolean visit(CompiledOperatorNode operator) {
			if (removeInline(operator))
				return true;
			// root = false;
			operator.getLeft().accept(this);
			Element left = element;
			operator.getRight().accept(this);
			Element right = element;
			ElementCode code = null;
			switch (operator.getOperator()) {
			case PLUS:
				code = ElementCode.ADD;
				break;
			case MINUS:
				code = ElementCode.SUBT;
				break;
			case DIVIDE:
				code = ElementCode.DIV;
				break;
			case MULTIPLY:
				code = ElementCode.MULT;
				break;
			case POWER:
				code = ElementCode.POW;
				break;
			default:

			}
			element = new Element(code);
			element.mFirstOperand = left;
			element.mSecondOperand = right;
			return true;
		}
		
		private boolean removeInline(CompiledExpression ce) {
			if (ce.hasExpandedForm() && (ce.returnExpandedForm() instanceof CompiledDynamicComponent)) {
				CompiledDynamicComponent cdc = (CompiledDynamicComponent) ce.returnExpandedForm();
				String cvName = cdc.getName();

				cvName = ce.returnExpandedForm().toString();

				if (sbaModel.inline(cvName)) {
					return false;
				}
				// remove inlined expression to avoid repeated evaluation
				return cdc.accept(this);
			}
			return false;
		}

		public Value getValue() {
			if (element.mCode.equals(ElementCode.NUMBER))
				return new Value(element.mNumericValue);
			return new Value(new Expression(element));
		}

		private final Element prep(SBAComponentBehaviour cb) {
			Element e = null;
			if (cb.stoichiometry > 1) {
				e = new Element(ElementCode.POW);
				e.mFirstOperand = new Element(ElementCode.SYMBOL);
				e.mFirstOperand.mSymbol = new Symbol(cb.getName());
				e.mSecondOperand = new Element(cb.stoichiometry);
			} else {
				e = new Element(ElementCode.SYMBOL);
				e.mSymbol = new Symbol(cb.getName());
			}
			return e;
		}

		public boolean visit(CompiledSystemVariable variable) {
			if (removeInline(variable))
				return true;
			switch (variable.getVariable()) {
			case TIME:
				element = new Element(ElementCode.SYMBOL);
				element.mSymbol = new Symbol(ReservedSymbolMapperChemCommandLanguage.SYMBOL_TIME);
				break;
			default:
				throw new IllegalStateException();
			}
			return true;
		}

	}
	
	static class Results extends BasicResult implements Result {

		String[] actionNames, componentNames;

		Map<String, Number> modelParameters = new HashMap<String, Number>(), uModelParameters = Collections
				.unmodifiableMap(modelParameters);
		String simulator;

		Map<String, Number> simulatorParameters = new HashMap<String, Number>(), uSimulatorParameters = Collections
				.unmodifiableMap(simulatorParameters);

		/* boolean throughput = false;
		double[] throughputValues, time;
		double[][] results; */
		private int[] resultHashes;
		// private int timeHashes;

		Results(SimulationResults results, boolean throughput, Parameters parameters,
				Map<String, Number> modelParameters, String simulator) {
			componentNames = results.getResultsSymbolNames();
			this.throughput = throughput;
			this.simulator = simulator;
			for (Map.Entry<Parameter, Object> me : parameters.parameters.entrySet())
				if (!me.getKey().equals(Parameter.Components) && me.getValue() instanceof Number)
					simulatorParameters.put(me.getKey().descriptiveName, (Number) me.getValue());
			for (Map.Entry<String, Number> me : modelParameters.entrySet())
				this.modelParameters.put(me.getKey(), me.getValue());
			timePoints = results.getResultsTimeValues();
			// timeHashes = Arrays.hashCode(timePoints);
			this.results = new double[componentNames.length][];
			for (int i = 0; i < componentNames.length; i++)
				this.results[i] = new double[timePoints.length];
			Object[] values = results.getResultsSymbolValues();
			double[] d;
			for (int i = 0; i < values.length; i++) {
				d = (double[]) values[i];
				for (int j = 0; j < d.length; j++)
					this.results[j][i] = d[j];
			}
			resultHashes = new int[componentNames.length];
			for (int i = 0; i < this.resultHashes.length; i++)
				resultHashes[i] = Arrays.hashCode(this.results[i]);
			if (throughput) {
				actionNames = results.getReactionNames();
				throughputValues = new double[actionNames.length];
				double[] times = results.getReactionTimes(), firings = results.getReactionCounts();
				for (int i = 0; i < times.length; i++)
					throughputValues[i] = (times[i] / firings[i]);

			}
		}

		public String[] getActionNames() {
			if (actionNames == null)
				return new String[] {};
			return actionNames.clone();
		}

		public double getActionThroughput(int index) {
			return throughputValues[index];
		}

		public String[] getComponentNames() {
			if (componentNames == null)
				return new String[] {};
			return componentNames.clone();
		}

		public String getSimulatorName() {
			return simulator;
		}

		public Map<String, Number> getSimulatorParameters() {
			return uSimulatorParameters;
		}

		public double[] getTimeSeries(int index) {
			// if (Arrays.hashCode(results[index]) != resultHashes[index])
			// 	throw new IllegalStateException("Time series has been modified since initial storage.");
			return results[index];
		}

		public boolean throughputSupported() {
			return throughput;
		}

		public double[] getTimePoints() {
			// if (Arrays.hashCode(timePoints) != timeHashes)
			// 	throw new IllegalStateException(" has been modified since initial storage.");
			return timePoints;
		}
	}

	private static Parameters odeParameters() {
		Parameters parameters = parameters();
		parameters.add(Parameter.Step_Size);
		parameters.add(Parameter.Absolute_Error);
		parameters.add(Parameter.Relative_Error);
		return parameters;
	}

	private static final Parameters parameters() {
		Parameters parameters = new Parameters();
		parameters.add(Parameter.Start_Time);
		parameters.add(Parameter.Stop_Time);
		parameters.add(Parameter.Data_Points);
		parameters.add(Parameter.Components);
		return parameters;
	}

	private static Parameters stochasticParameters() {
		Parameters parameters = parameters();
		parameters.add(Parameter.Independent_Replications);
		return parameters;
	}

	private Model model;

	Map<String, Number> parameterMap = new HashMap<String, Number>();

	private SBAModel sbaModel;

	private String[] toObserve;

	private SimulationController simulationController;

	private SimulationProgressReporter simulationProgressReporter;

	private Simulator simulator;

	private SimulatorParameters simulatorParameters;

	private Set<String> addedParameters;

	public ISBJava(SBAModel model, String[] toObserve) {
		sbaModel = model;
		this.toObserve = toObserve;
	}

	private Value generateRate(SBAReaction reaction, boolean reverse)
			throws BioPEPAException {
		RatesVisitor rv = new RatesVisitor();
		rv.reaction = reaction;
		rv.reversing = reverse;
		reaction.reactionRate.accept(rv);
		return rv.getValue();
	}

	private void mapModel() throws BioPEPAException {
		model = new Model();
		model.setReservedSymbolMapper(new ReservedSymbolMapperChemCommandLanguage());
		// Compartments
		Map<String, Compartment> compartments = new HashMap<String, Compartment>();
		Compartment compartment = new Compartment("main");
		compartments.put(null, compartment);
		addedParameters = new HashSet<String>();
		for (Map.Entry<String, Double> me : sbaModel.compartments.entrySet()) {
			compartment = new Compartment(me.getKey());
			compartment.setVolume(me.getValue());
			compartments.put(me.getKey(), compartment);
			parameterMap.put(me.getKey(), new Double(me.getValue()));
		}
		// Species
		Map<String, Species> speciesMap = new HashMap<String, Species>();
		Species species;
		CompartmentData cd;
		for (ComponentNode cn : sbaModel.getComponents()) {
			cd = cn.getCompartment();
			String componentName = cn.getName();
			double componentCount = cn.getCount();
			
			species = new Species(componentName, compartments.get(cd == null ? null : cd.getName()));
			species.setSpeciesPopulation(componentCount);
			speciesMap.put(componentName, species);
			parameterMap.put(componentName, new Double(componentCount));
		}
		// Reactions
		Reaction reaction;
		for (SBAReaction r : sbaModel.getReactions()) {
			if (r.isEnabled() /* && experimentLine.isReactionActiviated(r.getName())*/) {
				// System.out.println("reactionEnabled: " + r.getName());
				if (r.isReversible())
					reaction = new Reaction(r.forwardName);
				else
					reaction = new Reaction(r.name);
				for (SBAComponentBehaviour cb : r.reactants)
					reaction.addReactant(speciesMap.get(cb.getName()), cb.stoichiometry, cb.type.equals(Type.REACTANT));
				for (SBAComponentBehaviour cb : r.products)
					reaction.addProduct(speciesMap.get(cb.getName()), cb.stoichiometry);
				reaction.setRate(generateRate(r, false));
				model.addReaction(reaction);
				if (r.isReversible()) {
					reaction = new Reaction(r.reversibleName);
					for (SBAComponentBehaviour cb : r.products)
						reaction.addReactant(speciesMap.get(cb.getName()), cb.stoichiometry, true);
					for (SBAComponentBehaviour cb : r.reactants)
						reaction.addProduct(speciesMap.get(cb.getName()), cb.stoichiometry);
					reaction.setRate(generateRate(r, true));
					model.addReaction(reaction);
				}
			}
		}
		RatesVisitor rv;
		CompiledExpression ce;
		for (String s : toObserve) {
			if (addedParameters.contains(s))
				continue; // already added from traversing rates
			ce = sbaModel.getDynamicExpression(s);
			if (ce != null) {
				rv = new RatesVisitor();
				ce.accept(rv);
				model.addParameter(new org.systemsbiology.chem.Parameter(s, new Expression(rv.element)));
			}
		}
	}

	private SimulationResults run(Parameters requiredParameters, Parameters suppliedParameters,
			final ProgressMonitor monitor) throws BioPEPAException {
		simulationProgressReporter = new SimulationProgressReporter();
		simulator.setProgressReporter(simulationProgressReporter);
		simulationController = new SimulationController();
		simulator.setController(simulationController);
		if (!suppliedParameters.setOfKeys().containsAll(requiredParameters.setOfKeys()))
			throw new IllegalArgumentException("Incorrect parameters supplied.");
		// Set and check parameters
		int tInt, samples;
		double tDouble, startTime, stopTime;
		String[] species;
		// Required parameters
		startTime = (Double) suppliedParameters.getValue(Parameter.Start_Time);
		stopTime = (Double) suppliedParameters.getValue(Parameter.Stop_Time);
		if (startTime < 0.00 || startTime >= stopTime)
			throw new IllegalArgumentException("Start time < 0.00 || start time >= stop time.");
		samples = (Integer) suppliedParameters.getValue(Parameter.Data_Points);
		if (samples < 3)
			throw new IllegalArgumentException(Parameter.Data_Points.toString() + " must be greater than 2.");
		species = (String[]) suppliedParameters.getValue(Parameter.Components);
		if (species.length == 0)
			throw new IllegalArgumentException(Parameter.Components.toString()
					+ " must contain at least one component.");
		
		
		// TODO reinstate species checking. I don't believe this will return the
		// correct list.
		// HashSet<String> knownSpecies = new
		// HashSet<String>(Arrays.asList(model.getOrderedSpeciesNamesArray()));
		/*
		 * for (String component : species) if
		 * (!knownSpecies.contains(component)) { species = null; throw new
		 * IllegalArgumentException(Parameter.Components .toString() + ": " +
		 * component + " is not a valid selection."); }
		 */
		// Solver dependent parameters
		for (Parameter p : requiredParameters.arrayOfKeys()) {
			if (p.equals(Parameter.Step_Size)) {
				tDouble = (Double) suppliedParameters.getValue(p);
				if (tDouble <= 0.00)
					throw new IllegalArgumentException(p.toString() + " must be greater than 0.00.");
				simulatorParameters.setStepSizeFraction(tDouble);
			} else if (p.equals(Parameter.Independent_Replications)) {
				tInt = (Integer) suppliedParameters.getValue(p);
				if (tInt < 1)
					throw new IllegalArgumentException(p.toString() + " must be greater than 0.");
				simulatorParameters.setEnsembleSize(tInt);
			} else if (p.equals(Parameter.Relative_Error)) {
				tDouble = (Double) suppliedParameters.getValue(p);
				if (tDouble <= 0.00)
					throw new IllegalArgumentException(p.toString() + " must be greater than 0.00.");
				simulatorParameters.setMaxAllowedRelativeError(tDouble);
			} else if (p.equals(Parameter.Absolute_Error)) {
				tDouble = (Double) suppliedParameters.getValue(p);
				if (tDouble <= 0.00)
					throw new IllegalArgumentException(p.toString() + " must be greater than 0.00.");
				simulatorParameters.setMaxAllowedAbsoluteError(tDouble);
			}
		}
		// Prepare monitor thread
		simulationProgressReporter.setSimulationFinished(false);
		Thread monitorController = null;
		if (monitor != null) {
			monitorController = new Thread() {
				public void run() {
					try {
						int SCALING_UNIT = 100;
						int previous = 0, current = 0;
						monitor.beginTask(SCALING_UNIT);
						while (!simulationProgressReporter.getSimulationFinished()) {
							if (monitor.isCanceled())
								simulationController.setCancelled(true);
							simulationProgressReporter.waitForUpdate();
							current = (int) (simulationProgressReporter.getFractionComplete() * 100);
							monitor.worked(current - previous);
							previous = current;
						}
					} finally {
						monitor.done();
					}
				}
			};
			monitorController.start();
		}
		// Start simulator
		SimulationResults results = null;
		
		try {
			if (simulator instanceof SimulatorStochasticBase)
				results = ((SimulatorStochasticBase) simulator).simulate(startTime, stopTime, simulatorParameters,
						samples, species);
			else if (simulator instanceof SimulatorOdeToJavaBase)
				results = ((SimulatorOdeToJavaBase) simulator).simulate(startTime, stopTime, simulatorParameters,
						samples, species);
		} catch (Exception e) {
			throw new BioPEPAException(e.getMessage());
		}
		
		
		return results;
	}
}
