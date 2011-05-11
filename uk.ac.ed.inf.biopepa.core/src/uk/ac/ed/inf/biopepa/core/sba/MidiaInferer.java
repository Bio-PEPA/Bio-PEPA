package uk.ac.ed.inf.biopepa.core.sba;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;

public class MidiaInferer {
	
	private class Module {
		Set<String> residents;
		Set<String> mod_interface;
		
		Module (String r, Set<String> i){
			this.residents = new HashSet<String>();
			this.residents.add(r);
			this.mod_interface = i;
		}
	}
	
	Set<String> model_interface;
	Set<Module> model_modules;
	
	
	private void inferModules(SBAModel sbaModel){
		model_interface = new HashSet<String> ();
		model_modules = new HashSet<Module>();
		
		// Assume that the model is not null as it should be
		// checked by the caller, hence this method is private.
		
		// We begin by creating a module for every species in the model
		ComponentNode[] species = sbaModel.getComponents();
		for (ComponentNode comp : species){
			String compName = comp.getName();
			Set<String> connected = 
				AnalysisUtils.componentEdges(sbaModel, compName);
			Module compModule = new Module(compName, connected);
			model_modules.add(compModule);
			
		}
		
	}

	public SimpleTree[] createMidiaTree(SBAModel sbaModel){
		SimpleTree[] treearray = null;
	
		if(sbaModel == null) {
			treearray = new SimpleTree[1];
			treearray[0] = new SimpleTree();
			treearray[0].name = "Non-parseable Bio-PEPA model";
		} else {
			LinkedList<SimpleTree> treelist = new LinkedList<SimpleTree>();
			ComponentNode[] species = sbaModel.getComponents();
			
			
			for (ComponentNode comp : species){
				String compName = comp.getName();
				SimpleTree comptree = new SimpleTree(compName);
				Set<String> connected = 
					AnalysisUtils.componentEdges(sbaModel, compName);
				
				for (String edge : connected){
					SimpleTree edgeChild = new SimpleTree(edge);
					comptree.addChild(edgeChild);
				}
				
				treelist.add(comptree);
			}
			
			
			treearray = treelist.toArray(new SimpleTree[treelist.size()]);
		}
		
		return treearray;
	}
	
}
