package uk.ac.ed.inf.biopepa.core.sba;

import java.util.LinkedList;
import java.util.List;


public class CompRelTree {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CompRelTree (SBAReaction reaction){
		this.reaction = reaction;
		this.helps = new LinkedList<CompRelTree>();
		this.hinders = new LinkedList<CompRelTree>();
	}
	
	private SBAReaction reaction;
	private LinkedList<CompRelTree> helps;
	private LinkedList<CompRelTree> hinders;
	
	public String getName (){
		return this.reaction.getName();
	}
	
	public List<CompRelTree> getHelps(){
		return this.helps;
	}
	
	public List<CompRelTree> getHinders(){
		return this.hinders;
	}
	
	public void addHelps(CompRelTree h){
		this.helps.add(h);
	}
	
	public void addHinders(CompRelTree h){
		this.hinders.add(h);
	}

	public SimpleTree returnSimpleTree(){
		SimpleTree result = new SimpleTree(this.getName());
		SimpleTree helpsTree = new SimpleTree("helps");
		helpsTree.setParent(result);
		result.addChild(helpsTree);
		for (CompRelTree helped : this.helps){
			SimpleTree child = helped.returnSimpleTree();
			child.setParent(helpsTree);
			helpsTree.addChild(child);
		}
		
		SimpleTree hindersTree = new SimpleTree ("hinders");
		hindersTree.setParent(result);
		result.addChild(hindersTree);
		for (CompRelTree hindered : this.hinders){
			SimpleTree child = hindered.returnSimpleTree();
			child.setParent(hindersTree);
			hindersTree.addChild(child);
		}
		
		return result;
	}
	
	private boolean representsReaction (SBAReaction r){
		return this.reaction.equals(r);
	}
	
	public boolean doesHelp (SBAReaction r){
		for (CompRelTree rTree : this.helps){
			if (rTree.representsReaction(r) ||
					rTree.doesHelp(r)){
				return true;
			}
		}
		return false;
	}
}
