package uk.ac.ed.inf.biopepa.core.sba;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;

public class VennInference {
	private SBAModel sbaModel;
	public VennInference (SBAModel sbaModel){
		this.sbaModel = sbaModel;
	}
	
	public Collection<SimpleTree> inferVennTree (){
		// HashMap<String, SimpleTree> treeMap = new HashMap<String, SimpleTree>();
		LinkedList<SimpleTree> trees = new LinkedList<SimpleTree>();
		/*
		for (ComponentNode compNode : this.sbaModel.getComponents()){
			String compName = compNode.getName();
			SimpleTree compTree = new SimpleTree(compName);
			treeMap.put(compName, compTree);
		}
		*/
		
		for (SBAReaction reaction : sbaModel.getReactions()){
			List<SBAComponentBehaviour> reactants = reaction.getReactants();
			List<SBAComponentBehaviour> products = reaction.getProducts();
			
			// This should really be, actual reactants not including
			// activators etc. We should have 
			// AnalysisUtils.getConsumed(SBAReaction r)
			// and
			// AnalysisUtis.getProduced(SBAReaction r)
			if (reactants.size() == 1 && products.size() > 1){
				SBAComponentBehaviour reactant = reactants.get(0);
				SimpleTree rTree = new SimpleTree(reactant.getName());
				for (SBAComponentBehaviour prod : products){
					rTree.addNamedChild(prod.getName());
				}
				trees.add(rTree);
			}
			if (reactants.size() > 1 && products.size() == 1){
				SBAComponentBehaviour product = products.get(0);
				SimpleTree rTree = new SimpleTree(product.getName());
				for (SBAComponentBehaviour reactant : reactants){
					rTree.addNamedChild(reactant.getName());
				}
				trees.add(rTree);
			}
		}
		
		return trees;
	}
}
