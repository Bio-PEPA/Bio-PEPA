package uk.ac.ed.inf.biopepa.core.sba;

import java.util.ArrayList;
import java.util.List;


public class SimpleTree {
	String name, id;
	List<SimpleTree> children; 
	SimpleTree parent = null;
	
	private void initialise (){
		this.children = new ArrayList<SimpleTree>();
		this.parent = null;
	}
	
	SimpleTree (){
		this.initialise();
	}
	
	public SimpleTree (String name){
		this.initialise();
		this.setNameAndID(name);
	}
	
	public void setNameAndID(String name) {
		this.name = name;
		this.id = name;
	}
	
	public String getName (){
		return this.name;
	}
	
	public boolean equals(Object o) {
		if(o == null || !(o instanceof SimpleTree))
			return false;
		SimpleTree bpt = (SimpleTree) o;
		if(id == null || bpt.id == null || !id.equals(bpt.id))
			return false;
		return true;
	}
	
	public boolean hasChildren (){
		return children != null && !children.isEmpty();
	}
	
	public void setParent(SimpleTree p){
		this.parent = p;
	}
	
	public SimpleTree getParent (){
		return parent;
	}
	
	public SimpleTree[] getChildren(){
		return this.children.toArray(new SimpleTree[children.size()]);
	}
	
	public void addChild(SimpleTree child){
		this.children.add(child);
		child.setParent(this);
	}
	
	/*
	 * Use this to add a named child to the given simple tree.
	 * We return the newly created child tree such that it may
	 * itself have children added to it.
	 */
	public SimpleTree addNamedChild(String childName){
		SimpleTree child = new SimpleTree(childName);
		this.addChild(child);
		child.setParent(this);
		
		return child;
	}
	
	public int hashCode() {
		return id.hashCode();
	}
	
	public String printTree(){
		return this.printTree(0);
	}
	private void indentSB (LineStringBuilder lsb, int indent){
		for (int i = 0; i < indent; i++){
			lsb.append(" ");
		}
	}
	public String printTree(int indent){
		LineStringBuilder lsb = new LineStringBuilder();
		indentSB(lsb, indent);
		lsb.append(this.name);
		lsb.endLine();
		for (SimpleTree child : this.children){
			lsb.append(child.printTree(indent + 2));
		}
		
		return lsb.toString();
	}
}
