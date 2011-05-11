package uk.ac.ed.inf.biopepa.core.sba;

import java.util.LinkedList;
import java.util.List;

import uk.ac.ed.inf.biopepa.core.compiler.CompartmentData;
import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour.Type;

public class OutlineAnalyser {
		
	public SimpleTree[] createOutlineTree(SBAModel sbaModel){
		if(sbaModel == null) {
			SimpleTree[] treearray = new SimpleTree[1];
			treearray[0] = new SimpleTree();
			treearray[0].name = "Non-parseable Bio-PEPA model";
			return treearray;
		} else {			
			CompartmentData[] compartments = sbaModel.getCompartments();
			ComponentNode[] species = sbaModel.getComponents();
			SBAReaction[] reactions = sbaModel.getReactions();
			boolean needCompartmentTree = compartments.length != 0;
			
			/* We now set up a list of sources and sinks*/
			boolean[] speciesSources = new boolean[species.length];
			boolean[] speciesSinks = new boolean[species.length];
			int numberSources = 0;
			int numberSinks = 0;
			/* A species is a source if it a reactant is never
			 * anything but a reactant.
			 */
			for (int sIndex = 0; sIndex < species.length; sIndex++) {
				boolean isReactant = false;
				boolean isProduct = false;
				ComponentNode potentialReactant = species[sIndex];
				String sourceName = potentialReactant.getName();
				for (SBAReaction reaction : reactions) {
					for (SBAComponentBehaviour cb : reaction.getReactants()) {
						if (cb.getName().equals(sourceName) && cb.getType().equals(Type.REACTANT)) {
							isReactant = true;
						}

					}
					for (SBAComponentBehaviour cb : reaction.getProducts()) {
						if (cb.getName().equals(sourceName)) {
							isProduct = true;
						}

					}
					speciesSources[sIndex] = isReactant && !isProduct;
					speciesSinks[sIndex]   = !isReactant && isProduct;
				}
			}
			
			/* Now we just wish to count the number of sources and sinks */
			for (boolean sourceBool : speciesSources){
				if (sourceBool) { numberSources++ ; }
			}
			for (boolean sinkBool : speciesSinks){
				if (sinkBool){ numberSinks++ ; }
			}
			
			/*
			 * Now the sources and sinks we do the same but for actions
			 * so that we obtain action sources and actions sinks
			 * an action source is one that produces something
			 * "out of the ether" that is it has no reactant but some
			 * product
			 */
			LinkedList<SBAReaction> sourceActions = new LinkedList<SBAReaction>();
			LinkedList<SBAReaction> sinkActions = new LinkedList<SBAReaction>();
			
			for(SBAReaction reaction : reactions){
				List<SBAComponentBehaviour> reactants = reaction.getReactants();
				List<SBAComponentBehaviour> products = reaction.getProducts();
				
				/* So if there are no products then the reaction is said to be
				 * a sink
				 */
				boolean isSink = false;
				boolean isSource = true;
				for (SBAComponentBehaviour cb : reactants){
					if (cb.getType().equals(Type.REACTANT)) {
						// to be source it must have no reactants
						isSource = false;
						// to be a sink it must have at least one
						// reactant.
						isSink = true;
						break;
					}
				}
				// To be a sink action it must have at least one
				// reactant which we have checked above and the products
				// must be empty.
				if (isSink && products.isEmpty()){
					sinkActions.add(reaction);
				}
				// To be a source there must be no reactants which we
				// have checked above and there must be at least one
				// product.
				if (isSource && !products.isEmpty()){
					sourceActions.add(reaction);
				}
			}
			
			// We build up a list of simple trees and then simply turn
			// that into an array of simple trees.
			LinkedList<SimpleTree> treelist = new LinkedList<SimpleTree>();

			if (needCompartmentTree) {
				String name = compartments.length + " Location" + 
				              (compartments.length > 1 ? "s" : "");
				SimpleTree cTree = new SimpleTree(name);
				treelist.addLast(cTree);
				cTree.id = "Locations";
				//for (int i = 0; i < compartments.length; i++) {
				for (CompartmentData cd : compartments){
					// cd = compartments[i];
					SimpleTree tree = new SimpleTree(cd.getName());
					cTree.addChild(tree);
					String infoName = cd.getType().toString();
					SimpleTree infoTree = new SimpleTree(infoName);
					tree.addChild(infoTree);
					String volname = "Volume = " + cd.getVolume();
					SimpleTree volTree = new SimpleTree(volname);
					tree.addChild(volTree);
					if(!Double.isNaN(cd.getStepSize())) {
						String stepName = "Step-size = " + cd.getStepSize();
						SimpleTree stepTree = new SimpleTree(stepName);
						tree.addChild(stepTree);
					}
				}
			}
			
			String speciesName = species.length + " Species";
			SimpleTree speciesTree = new SimpleTree(speciesName);
			speciesTree.id = "Species";
			treelist.addLast(speciesTree);
			
			String lastname = null;
			// This initialisation is redundant and is only here
			// to avoid errors about a possibly uninitialised variable.
			// However because on the first iteration of the for loop
			// lastname is null, we know that the
			// if (componentName.equals(lastname))
			// will be false and hence this will be 'reinitialised' on the
			// first iteration of the for loop.
			SimpleTree compTree = new SimpleTree();
			for (int i = 0; i < species.length; i++) {
				ComponentNode component = species[i];
				String componentName = component.getComponent();
				
				/*
				 * We only want one tree for each species, but there
				 * will be more than one component node if the species
				 * exists in more than one compartment. Hence we only
				 * create a new componentTree if we are not just adding
				 * to the previous one.
				 */
				if(!componentName.equals(lastname)) {
					compTree = new SimpleTree(componentName);
					speciesTree.addChild(compTree);
					lastname = componentName;
				}
				StringBuilder sb = new StringBuilder();
				if(needCompartmentTree) {
					sb.append("in ");
					sb.append(component.getCompartment().getName());
					sb.append(" ");
				}
				sb.append("with initial #molecules = ");
				sb.append(component.getCount());
				if (speciesSources[i]){
					sb.append(" (is-source)");
				}
				if (speciesSinks[i]){
					sb.append(" (is-sink)");
				}
				SimpleTree initTree = new SimpleTree(sb.toString());
				compTree.addChild(initTree);
				
				// Underneath the tree of the component we add a
				// leaf for ever reaction in which it is involved.
				// Originally I had a single tree which you could
				// expand to show all the reactions but I think expanding
				// the component node is enough, generally each component
				// in only involved in a few reactions.
				for (SBAReaction reaction : reactions){
					String involvedName = component.getName();
					if (AnalysisUtils.compInvolvedInReaction(involvedName, 
																reaction)){
						SimpleTree rTree = new SimpleTree(reaction.toString());
						compTree.addChild(rTree);
					}
				}
				
			}
			/*
			 * At this point we used to have a for loop which would look
			 * through all the comptrees and see which had more than one
			 * child. This would mean that there were several species in
			 * different locations had the same name and were put under the
			 * same tree. We can't do that now because we have the reactions
			 * as children as well. I'm not sure how worthwhile it is anyway.
			 * If we really want to we can keep a count of how many species
			 * are under each component name and update the titles here
			 * accordinly, but I'm not convinced it is worth it.
			 */
			
			/* Reactions Tree */
			String rTreeName = reactions.length + " Reaction" + 
			 					(reactions.length > 1 ? "s" : "");
			SimpleTree reactionsTree = new SimpleTree (rTreeName);
			reactionsTree.id = "Reactions";
			treelist.addLast(reactionsTree);
			for (SBAReaction reaction : reactions) {				
				String enabled ;
				if (reaction.isEnabled()){
					enabled = "";
				} else { enabled = "     disabled"; }
				String name = reaction.toString() + enabled;
				SimpleTree rTree = new SimpleTree (name);
				reactionsTree.addChild(rTree);
			}
			
			/* Source and Sinks Tree, first Sources */
			if (numberSources > 0) {
				String name = numberSources + " Sources";
				SimpleTree sourcesTree = new SimpleTree (name);
				sourcesTree.id = "Sources";
				treelist.addLast(sourcesTree);
				for (int sIndex = 0; sIndex < species.length; sIndex++) {
					if (speciesSources[sIndex]) {
						ComponentNode comp = species[sIndex];
						String sourceName = comp.getName();
						sourcesTree.addNamedChild(sourceName);
					}
				}
			}
			/* Second sinks */
			if (numberSinks > 0) {
				String name = numberSinks + " Sinks";
				SimpleTree sinksTree = new SimpleTree(name);
				sinksTree.id = "Sinks";
				treelist.addLast(sinksTree);
				for (int sIndex = 0; sIndex < species.length; sIndex++) {
					if (speciesSinks[sIndex]) {
						ComponentNode comp = species[sIndex];
						sinksTree.addNamedChild(comp.getName());
					}
				}
			}
			/*
			 * The same again for source actions and for sink actions.
			 */
			if (sourceActions.size() > 0){
				String sourceName = sourceActions.size() + " source actions";
				SimpleTree sourcesTree = new SimpleTree (sourceName);
				sourcesTree.id = "source actions";
				treelist.addLast(sourcesTree);
				for (SBAReaction reaction : sourceActions){
					String name = reaction.toString();
					sourcesTree.addNamedChild(name);
				}
			}
			// Same again for the sink actions
			if (sinkActions.size() > 0){
				String sinksName = sinkActions.size() + " sink actions";
				SimpleTree sinksTree = new SimpleTree(sinksName);
				treelist.addLast(sinksTree);
				sinksTree.id = "sink actions";
				for (SBAReaction reaction : sinkActions){
					String name = reaction.toString();
					sinksTree.addNamedChild(name);
				}
			}
			
			// Finally (within the 'else') return the array of trees.
			return treelist.toArray(new SimpleTree[treelist.size()]);
		}
	}
}
