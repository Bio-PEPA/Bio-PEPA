/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.analysis;

import java.util.*;
import java.util.Map.Entry;

import uk.ac.ed.inf.biopepa.core.compiler.*;
import uk.ac.ed.inf.biopepa.core.compiler.PrefixData.Operator;
import uk.ac.ed.inf.biopepa.core.compiler.ProblemInfo.Severity;

public class ReactantRateParticipationCheck {

	private ModelCompiler compiledModel;
	private SystemEquationVisitor sev = new SystemEquationVisitor();
	private ReactantParticipantVisitor rrp = new ReactantParticipantVisitor();
	private List<ProblemInfo> problems = new ArrayList<ProblemInfo>();

	private ReactantRateParticipationCheck() {
	}

	static List<ProblemInfo> checkActions(ModelCompiler compiledModel) {
		ReactantRateParticipationCheck rrpc = new ReactantRateParticipationCheck();
		rrpc.compiledModel = compiledModel;
		rrpc.sev.visit(compiledModel.getSystemEquation());
		rrpc.checkExternalParticipants();
		return rrpc.problems;
	}

	public void checkExternalParticipants(){
		// Get all the functional rates and
		// make a map from action names to sets of external participants
		HashMap<FunctionalRateData, HashSet<String>> actionMap = 
			new HashMap<FunctionalRateData, HashSet<String>> ();
		Map<String, FunctionalRateData> frdMap = 
			compiledModel.getFunctionalRateMap();
		for (Entry<String, FunctionalRateData> entry : frdMap.entrySet()){
			FunctionalRateData rateData = entry.getValue();
			ExternalParticipationVisitor epv = 
				new ExternalParticipationVisitor();
			rateData.getRightHandSide().accept(epv);
			actionMap.put(rateData, epv.getParticipants());
		}
		// Check each component definition; for every action name
		// the component should either *not* appear in the set of external
		// participants OR should contain the action in its definition.
		for (ComponentData compdata : compiledModel.getComponents()){
			// The set of reactions that the component is a reactant in
			HashSet<String> isReactants = componentReactantReactions(compdata);
			// So for every reaction we check if this component
			// is an external participant, that is does it affect the rate
			// other than through fMA etc.
			for (Entry<FunctionalRateData, HashSet<String>> entry : actionMap.entrySet()){
				// if component affects the rate of reaction
				// and component is not a reaction
				FunctionalRateData rateData = entry.getKey();
				String reactionName = rateData.getName();
				String compName = compdata.getName();
				if (entry.getValue().contains(compName) &&
						!isReactants.contains(reactionName)){
					ProblemInfo pi = 
						new ProblemInfo("The component " + compName + 
								" affects the rate of the reaction " +
								reactionName + " but is not a reactant",
								// Not sure where to report the error
								// either at the rate definition or the
								// component definition, we currently opt
								// for the latter because then multiple such
								// warning are in the correct order (since
								// here we are iterating through the component
								// definitions).
								compdata);
					pi.severity = Severity.WARNING;
					problems.add(pi);
				}
			}
		}
	}
	
	/*
	 * Given a component definition return all the reactions in which
	 * the component is a reactant.
	 */
	private HashSet<String> componentReactantReactions(ComponentData cd){
		HashSet<String> reactions = new HashSet<String> ();
		for (PrefixData prefix : cd.getPrefixes()){
			if (prefix instanceof ActionData){
				ActionData actionData = (ActionData) prefix;
				/*
				 * I think this is true even if it is an enzyme, but check?
				 * And also for general modifier and inhibitor.
				 */
				Operator operator = actionData.getOperator();
				if (operator.equals(Operator.REACTANT) ||
						operator.equals(Operator.ACTIVATOR) ||
						operator.equals(Operator.INHIBITOR) ||
						operator.equals(Operator.GENERIC) ){
					reactions.add(actionData.getFunction());
				}
			}
		}
		
		return reactions;
	}
	
	private class SystemEquationVisitor {

		void visit(SystemEquationNode node) {
			if (node instanceof ComponentNode)
				visit((ComponentNode) node);
			else if (node instanceof CooperationNode) {
				// Could have been placed in its own call but seems a little
				// over the top
				visit(((CooperationNode) node).getLeft());
				visit(((CooperationNode) node).getRight());
			} else
				throw new IllegalArgumentException("Unrecognised subclass of SystemEquationNode.");
		}

		void visit(ComponentNode node) {
			ComponentData cd = compiledModel.getComponentData(node.getComponent());
			ActionData ad;
			FunctionalRateData frd;
			CompartmentData compartmentData = node.getCompartment();
			ProblemInfo pi;
			for (PrefixData pd : cd.getPrefixes()) {
				if (pd instanceof ActionData) {
					ad = (ActionData) pd;
					if (ad.getOperator().equals(Operator.REACTANT)) {
						frd = compiledModel.getFunctionalRate(pd.getFunction());
						/*
						 * Importantly we must check for null because
						 * it could be that the rate function isn't defined
						 * at all which will be caught by an earlier check
						 * but this check is still performed.
						 */
						if (frd == null){
							/* 
							 * We actually don't need to report this since
							 * it should be else where reported
							 */
							pi = new ProblemInfo ("The rate " +
									pd.getFunction() + " is not defined but used in " +
							 		node.toString(),pd);
							problems.add(pi);
						} else if (!frd.isPredefinedLaw()) {
							if (ad.getLocations().size() == 0 || compartmentData == null
									|| ad.getLocations().contains(compartmentData.getName())) {
								rrp.reactant = node.getName();
								if (!frd.getRightHandSide().accept(rrp)) {
									pi = new ProblemInfo("The rate " + pd.getFunction() + " does not rely on "
											+ rrp.reactant + ". Population count could decrease below zero.", pd);
									pi.severity = Severity.WARNING;
									problems.add(pi);
								}
							}
						}
					}
				}
			}
		}
	}

	private class ReactantParticipantVisitor extends CompiledExpressionVisitor {

		String reactant;

		@Override
		public boolean visit(CompiledDynamicComponent component) {
			return component.getName().equals(reactant);
		}

		@Override
		public boolean visit(CompiledFunction function) {
			boolean found = false;
			for (CompiledExpression ce : function.getArguments())
				found = found || ce.accept(this);
			return found;
		}

		@Override
		public boolean visit(CompiledNumber number) {
			return false;
		}

		@Override
		public boolean visit(CompiledOperatorNode operator) {
			/*
			 * We'd prefer to do the simple thing of just
			 * descending into both expressions however either
			 * may be null. If indeed they are null then it is
			 * likely due to an error which should be detected
			 * earlier.
			 */
			CompiledExpression left  = operator.getLeft();
			CompiledExpression right = operator.getRight();
			boolean leftBool = false ;
			boolean rightBool = false ;
			if (left != null){
				leftBool = left.accept(this);
			}
			if (right != null){
				rightBool = right.accept(this);
			}
			
			return (leftBool || rightBool);
		}

		@Override
		public boolean visit(CompiledSystemVariable variable) {
			return false;
		}
	}
	
	/*
	 * In contrast to the class above: ReactantParticipantVisitor
	 * this visitor builds up a list of reactants which are mentioned
	 * in the functional rate. This is in order to implement the check
	 * "Does a reaction's rate depend on the population of a variable
	 * which is not involved in the reaction". Therefore we can safely
	 * ignore the predefined rate laws. This means that you cannot use
	 * this visitor for other means, because it will not return all
	 * the species populations which affect the rate since it won't
	 * return all the reactants when faced with a predefined law
	 * such as 'fMA'. In particular it cannot be used to implement
	 * the above, although in theory we could implement them both at
	 * the same time.
	 */
	private class ExternalParticipationVisitor extends CompiledExpressionVisitor {
		HashSet <String> participants;
		
		ExternalParticipationVisitor (){
			this.participants = new HashSet<String>();
		}
		
		public HashSet<String> getParticipants(){
			return this.participants;
		}
		
		@Override
		public boolean visit(CompiledDynamicComponent component) {
			participants.add(component.getName());
			return false;
		}

		@Override
		public boolean visit(CompiledFunction function) {
			for (CompiledExpression ce : function.getArguments()){
				ce.accept(this);
			}
			return false;
		}

		@Override
		public boolean visit(CompiledNumber number) {
			return false;
		}

		@Override
		public boolean visit(CompiledOperatorNode operator) {
			/*
			 * We'd prefer to do the simple thing of just
			 * descending into both expressions however either
			 * may be null. If indeed they are null then it is
			 * likely due to an error which should be detected
			 * earlier.
			 */
			CompiledExpression left  = operator.getLeft();
			CompiledExpression right = operator.getRight();
			if (left != null) { left.accept(this); }
			if (right != null) { right.accept(this); }
			return false;
		}

		@Override
		public boolean visit(CompiledSystemVariable variable) {
			return false;
		}
	}
	
}
