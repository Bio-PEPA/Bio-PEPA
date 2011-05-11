package uk.ac.ed.inf.biopepa.core.sba;

import java.util.LinkedList;
import java.util.Set;

import uk.ac.ed.inf.biopepa.core.analysis.ReactantsParticipantsVisitor;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour.Type;

public class ComponentRelationsInferer {
	// private SBAModel sbaModel;
	// private CompartmentData[] compartments;
	// private ComponentNode[] species;
	private SBAReaction[] reactions;

	public ComponentRelationsInferer(SBAModel sbaModel,
			LinkedList<SBAReaction> reactions) {
		// this.sbaModel = sbaModel;
		// this.compartments = sbaModel.getCompartments();
		// this.species = sbaModel.getComponents();
		// this.reactions = sbaModel.getReactions();
		this.reactions = reactions.toArray(new SBAReaction[0]);
	}

	

	SimpleTree relationsSimpleTree;
	public SimpleTree getRelationsTree(){
		return this.relationsSimpleTree;
	}

	public void computeComponentRelations() {
		CompRelTree[] reactionTrees = new CompRelTree[reactions.length];
		for (int index = 0; index < reactions.length; index++) {
			reactionTrees[index] = new CompRelTree(reactions[index]);
		}
		// relationsMap = new CompRelMap ("");

		// Set up the initial trees based on those reactions
		// which directly help each other
		for (int index = 0; index < reactions.length; index++) {
			SBAReaction reaction = reactions[index];
			CompRelTree rTree = new CompRelTree(reaction);
			reactionTrees[index] = rTree;

			for (SBAReaction helpee : reactions) {
				if (!helpee.equals(reaction)) {
					if (AnalysisUtils.reactionHelps(reaction, helpee)) {
						CompRelTree hTree = new CompRelTree(helpee);
						rTree.addHelps(hTree);
					}
					if (AnalysisUtils.reactionHinders(reaction, helpee)) {
						CompRelTree hTree = new CompRelTree(helpee);
						rTree.addHinders(hTree);
					}
				}
			}
		}

		/*
		 * Finally convert the reactions relations' trees to simple trees and
		 * combine them all into a single tree with a root node with as many
		 * children as there are reactions.
		 */
		relationsSimpleTree = new SimpleTree("");
		for (int index = 0; index < reactionTrees.length; index++) {
			SimpleTree child = reactionTrees[index].returnSimpleTree();
			relationsSimpleTree.addChild(child);
			child.setParent(relationsSimpleTree);
		}

	}
}
