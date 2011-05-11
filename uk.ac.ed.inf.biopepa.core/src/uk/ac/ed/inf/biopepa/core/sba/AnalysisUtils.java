package uk.ac.ed.inf.biopepa.core.sba;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ed.inf.biopepa.core.analysis.ReactantsParticipantsVisitor;
import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour.Type;

public class AnalysisUtils {

	
	/*
	 * Returns the component behaviour of the given component if it is
	 * a reactant in the given reaction and otherwise returns null.
	 */
	public static SBAComponentBehaviour reactantBehaviour(String component,
														  SBAReaction reaction){
		
		for (SBAComponentBehaviour cb : reaction.getReactants()){
			if (cb.getName().equals(component)){
				return cb;
			}
		}
		
		return null;
	}
													
	// Returns true if the given component is a reactant in the given
	// reaction.
	public static boolean componentIsReactant(String component,
			                                  SBAReaction reaction){
		return null != reactantBehaviour(component, reaction);
	}
	
	/*
	 * As reactantBehaviour above, returns the component behaviour for
	 * the given component if it is a product in the given reaction and
	 * otherwise null
	 */
	public static SBAComponentBehaviour productBehaviour(String component,
														 SBAReaction reaction){
		for (SBAComponentBehaviour cb : reaction.getProducts()){
			if (cb.getName().equals(component)){
				return cb;
			}
		}
		return null;
	}
	
	/*
	 * Returns true if the component is a reactant that does not lose
	 * population 
	 */
	
	/*
	 * Returns true if the given component is a product of the given reaction.
	 */
	public static boolean componentIsProduct(String component,
			                                 SBAReaction reaction){
		return null != productBehaviour(component, reaction);
	}
	
	/*
	 * Returns the component behaviour of the given component if it
	 * is involved in the given reaction (that is if it is either or both
	 * a product or reactant of the given reaction) and null otherwise.
	 * 
	 * This has the plausible problem that if a component has more than
	 * one behaviour associated with a given reaction it will only return
	 * the first such. Currently it is illegal in BioPEPA to have more than
	 * one, but I'm not sure how well that is enforced nor whether it is
	 * likely to remain, as some people do like reactions such as:
	 * A + B --> B + B
	 */
	public static SBAComponentBehaviour involvedBehaviour(String component,
			 											SBAReaction reaction){
		SBAComponentBehaviour cb = reactantBehaviour(component, reaction);
		if (cb != null){
			return cb;
		}
		return productBehaviour(component, reaction);
	}
	
	/*
	 * Returns true if the component is either or both a reactant or
	 * a product in the given reaction.
	 */
	public static boolean compInvolvedInReaction(String component, 
												 SBAReaction reaction){
		if (componentIsReactant(component, reaction)){
			return true;
		}
		if (componentIsProduct(component, reaction)){
			return true;
		}
		
		return false;
	}
	
	
	public static boolean rateAffected(SBAReaction r, String compName) {
		// First we check if the rate does include all of the
		// reactants via a rate law, this is the common situation.
		// If so then we simply need to check if the component is
		// a reactant.
		ReactantsParticipantsVisitor rpv = new ReactantsParticipantsVisitor();
		r.getRate().accept(rpv);
		if (rpv.getReactantsInvolved()) {
			for (SBAComponentBehaviour rb : r.getReactants()) {
				if (rb.getName().equals(compName)) {
					return true;
				}
			}
		}
		// If either the component isn't a reactant or a rate
		// law is not used, then we simply need to check if
		// the component affects the rate, we assume that it
		// affects the rate in a positive way, so we are wrong
		// if from example the rate E - V, but such rates are
		// unusual.
		return rpv.getExternalParticipants().contains(compName);
	}

	/*
	 * Returns the set of components which affect the rate of the
	 * given reaction. 
	 */
	public static Set<String> reactionRateModifiers(SBAReaction r) {
		ReactantsParticipantsVisitor rpv = new ReactantsParticipantsVisitor();
		r.getRate().accept(rpv);
		Set<String> results = rpv.getExternalParticipants();
		if (rpv.getReactantsInvolved()) {
			for (SBAComponentBehaviour reactant : r.getReactants()) {
				results.add(reactant.getName());
			}
		}

		return results;
	}

	/*
	 * Returns the net gain for a given component of the firing of the given
	 * reaction. This will be negative if the given component is consumed and
	 * positive if it is produced. It will be zero if the component is neither
	 * produced nor consumed, eg: A + B -> A + C will have 0 for A, -1 for B and
	 * +1 for C.
	 */
	public static int netGainForReaction(SBAReaction reaction, String comp) {
		int netGain = 0;

		// First decrease the netGain for any molecules of the given
		// component which are consumed.
		for (SBAComponentBehaviour reactant : reaction.getReactants()) {
			if (reactant.getType().equals(Type.REACTANT) && 
					reactant.getName().equals(comp)) {
				netGain -= reactant.getStoichiometry();
			}
		}

		// Then increase the netGain for any molecules of the given
		// component which are produced.
		for (SBAComponentBehaviour product : reaction.getProducts()) {
			if (product.getName().equals(comp)) {
				netGain += product.getStoichiometry();
			}
		}

		return netGain;
	}

	// Returns true if the given reaction actually consumes the given
	// component behaviour
	public static boolean reactionConsumes(SBAReaction r,
			String reactant) {
		int netGain = netGainForReaction(r, reactant);
		return netGain < 0;
	}

	// Returns true if the given reaction actually produces the given
	// component behaviour, should be called with
	// for (product : r.getProducts()) { (r, product) }
	public static boolean reactionProduces(SBAReaction r,
			String product) {
		int netGain = netGainForReaction(r, product);
		return netGain > 0;
	}
	
	// Returns true if the given reaction produces or consumes any
	// number of molecules of the given component.
	public static boolean reactionModifiesPopulation(SBAReaction r,
			String compName){
		int netGain = netGainForReaction(r, compName);
		return netGain != 0;
	}

	public static boolean reactionHelps(SBAReaction helper,
			SBAReaction helpee) {
		// A reaction directly helps another if any of the
		// species is produced by the reaction which positively
		// affect the rate of the helpee.
		Set<String> rateAffectors = reactionRateModifiers(helpee);
		for (String reactant : rateAffectors) {
			if (reactionProduces(helper, reactant)) {
				return true;
			}
		}
		// If none of the reactants of the helpee are produced
		// by the helper then it doesn't indeed help it.
		return false;
	}

	public static boolean reactionHinders(SBAReaction hinderer, 
			SBAReaction hinderee) {
		// A reaction directly hinders another if it consumes
		// any of the hindered reaction's reactants, when we say
		// reactants we mean any component which positively affect
		// the rate of the hinderee
		Set<String> rateAffectors = reactionRateModifiers(hinderee);
		for (String reactant : rateAffectors) {
			if (reactionConsumes(hinderer, reactant)) {
				return true;
			}
		}
		// If none of the supposedly hindered reactants are
		// consumed by the hinderer reaction then there isn't
		// a direct hindered relationship (in that direction).
		return false;
	}
	
	/*
	 * Returns true if the KIG graph (see below) should have an edge
	 * between the two given components. 
	 * Essentially there is an (undirected) edge between components A and B
	 * if there exists a reaction 'r' such that either A affects the rate of
	 * 'r' and 'r' changes the population of B, or vice versa.
	 */
	public static boolean componentsConnected(SBAModel model,
			String compOne, String compTwo){
		for (SBAReaction reaction : model.getReactions()){
			boolean aAffectsR = rateAffected(reaction, compOne);
			boolean rAdjustsB = reactionModifiesPopulation(reaction, compTwo);
			boolean bAffectsR = rateAffected(reaction, compTwo);
			boolean rAdjustsA = reactionModifiesPopulation(reaction, compOne);
		
			if ( (aAffectsR && rAdjustsB) ||
					(bAffectsR && rAdjustsA)){
				return true;
			}
		}
		
		// If we have not returned true then there should be no edge
		return false;
	}
	
	/*
	 * Returns the components to which the given component is connected
	 * in the KIG graph (see MIDIA algorithm, software, google Clive Bowsher)
	 * Essentially there is an (undirected) edge between components A and B
	 * if there exists a reaction 'r' such that either A affects the rate of
	 * 'r' and 'r' changes the population of B, or vice versa.
	 */
	public static Set<String> componentEdges(SBAModel model, String compName){
		Set<String> results = new HashSet<String>();
		for (ComponentNode compnode : model.getComponents()){
			// Ignore the self loop edge which almost every component
			// will have, unless it is only an enzyme (ie. its population
			// count does not change).
			String candidateName = compnode.getName();
			if (!candidateName.equals(compName) &&
					componentsConnected(model, compName, candidateName)){
				results.add(candidateName);
			}
		}
		return results;
	}
	
}
