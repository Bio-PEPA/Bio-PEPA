package uk.ac.ed.inf.biopepa.core.imports;

public class NetworKinLine {

	private String kinase;
	private String protein;
	private String residue;
	public NetworKinLine (String kinase, String protein, String residue){
		this.kinase = kinase;
		this.protein = protein;
		this.residue = residue;
	}
	
	public String getKinase (){
		return this.kinase;
	}
	
	public String getProtein (){
		return this.protein;
	}
	
	public String getResidue (){
		return this.residue;
	}
	
	public String toNarrative (){
		return this.kinase + " phosphorylates " +
		       this.protein + " on residue " +
		       this.residue;
	}
	
}
