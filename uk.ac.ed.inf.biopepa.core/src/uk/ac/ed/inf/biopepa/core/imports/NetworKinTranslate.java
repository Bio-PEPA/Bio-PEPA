package uk.ac.ed.inf.biopepa.core.imports;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import uk.ac.ed.inf.biopepa.core.sba.LineStringBuilder;

/*
 * This module attempts to translate a series of
 * NetworKinLines into a working biopepa model.
 * Generally this doesn't quite work but we can at least
 * generate a syntactically correct model. 
 */
public class NetworKinTranslate {
	
	private List<NetworKinLine> networkLines;
	public NetworKinTranslate (List<NetworKinLine> lines){
		this.networkLines = lines;
	}
	
	/*
	 * Ultimately this should produce an ast Model
	 * but for now we will return a string since we'll start
	 * by producing some intermediate results.
	 */
	public void translate (){
		// Composites are represented as sets, in this way
		// by printing a composite we can make sure that two
		// composites are the same if they are composed of the
		// same molecules, that is we do not suffer from producing
		// both: K1:K2:P and K2:K1:P
		
		// First we build up a mapping from composite names to proteins
		HashMap<String, Protein> proteinMap = 
			new HashMap<String, Protein>();
		
		// Each protein consists of a mapping from residues to the
		// the set of kinases which may attach there.
		for (NetworKinLine nline : this.networkLines){
			Composite kinase = new Composite (nline.getKinase());
			String residue = nline.getResidue();
			
			String proteinName = nline.getProtein();
			Composite protein = new Composite (proteinName);
			
			Protein proteinValue = proteinMap.get(proteinName);
			if (proteinValue == null){
				proteinValue = new Protein(protein);
				proteinMap.put(proteinName, proteinValue);
			}
			proteinValue.addKinaseAtResidue(kinase, residue);
		}
		
		// Now we build up a set of reactions, a reaction essentially
		// combines a protein and a kinase at a given residue, for 
		// now we'll just build up a reaction for each possible binding
		// later we'll see that we need more, to account for the composites
		// made via such reactions which can still participate in such
		// reactions, for that I think I'll need a stack, anyway for now
		// do the easiest thing possible.
		reactions = new LinkedList<Reaction>();
		Stack <Protein> proteinStack = new Stack <Protein>();
		for (Entry<String, Protein> entry : proteinMap.entrySet()){
			proteinStack.add(entry.getValue());
		}
		while (!proteinStack.isEmpty()){
			Protein protein = proteinStack.pop();
			Composite proteinComposite = protein.getComposite();
			for (Entry <String, HashSet<Composite>> residueEntry : 
					protein.residueMap.entrySet()){
				String residue = residueEntry.getKey();
				// int number = 0;
				for (Composite kinase : residueEntry.getValue()){
					HashSet<Composite> reactants = new HashSet<Composite>();			
					HashSet<Composite> products = new HashSet<Composite>();
			
					String kName = kinase.printSyntax();
					String reactionName = "attach_" + kName + "_" + residue; 
											// + "_" + number;
					reactants.add(proteinComposite);
					reactants.add(kinase);
					
					Protein productProtein = 
						protein.attachKinaseAtResidue(kinase, residue);
					Composite product = productProtein.getComposite();
					products.add(product);
					Reaction r = 
						new Reaction (reactionName, reactants, products);
					reactions.addLast(r);
					// number++;
					proteinStack.add(productProtein);
				}
			}
		}
		
		// Translating reactions into biopepa components is not
		// so difficult. We keep a mapping from composites to their
		// biopepa component, for each reaction, we add it to the reactants
		// and products by first looking up the composite in the new mapping
		// and creating one if it isn't there.
		HashMap<Composite, BioPEPAComponent> componentMap = 
			new HashMap<Composite, BioPEPAComponent> ();
		for (Reaction reaction : reactions){
			String reactionName = reaction.getName();
			for (Composite reactant : reaction.getReactants()){
				BioPEPAComponent reactantComp = componentMap.get(reactant);
				if (reactantComp == null){
					reactantComp = new BioPEPAComponent(reactant);
					componentMap.put(reactant, reactantComp);
				}
				reactantComp.addReactantReaction(reactionName);
			}
			// Do (almost) the exact same for the products.
			for (Composite product : reaction.getProducts()){
				BioPEPAComponent reactantComp = componentMap.get(product);
				if (reactantComp == null){
					reactantComp = new BioPEPAComponent(product);
					componentMap.put(product, reactantComp);
				}
				reactantComp.addProductReaction(reactionName);
			}
		}
		
		this.biopepaComponents = componentMap.values();
		return;
	}
	
	// These are set by translate, and should not examined until
	// translate has been called.
	private LinkedList<Reaction> reactions;
	private Collection<BioPEPAComponent> biopepaComponents;
	
	/*
	 * Should not be called until this.translate has been called.
	 */
	public String getBioPepaString (){
		LineStringBuilder sb = new LineStringBuilder ();
		
		for (BioPEPAComponent component : this.biopepaComponents){
			sb.appendLine(component.printSyntax());
		}
		
		return sb.toString();
	}
	/*
	 * Should not be called until this.translate has been called.
	 */
	public String reactionsString (){
		LineStringBuilder sb = new LineStringBuilder ();
		
		for (Reaction reaction : this.reactions){
			sb.appendLine(reaction.printSyntax());
		}
		
		return sb.toString();
	}
	

	
	
	
	// A protein consists of a mapping from residue names
	// to sets of Kinase names which may phosphorylate the protein
	// on that particular residue
	private class Protein {
		HashMap<String, HashSet<Composite>> residueMap;
		Composite composite;
		
		Protein (Composite name){
			this.composite = name;
			this.residueMap = new HashMap<String, HashSet<Composite>> ();
		}
		
		public Composite getComposite(){
			return this.composite;
		}
		
		/*
		 * This is for use when building up the definition of the protein
		 * We build up a mapping of residues associated with the protein,
		 * to sets of kinases which may attach to the protein at that
		 * residue.
		 */
		public void addKinaseAtResidue(Composite kinase, String residue){
			HashSet<Composite> kinases = this.residueMap.get(residue);
			
			// If there is currently no set in the map corresponding to
			// the given residue, put one in.
			if (kinases == null){
				kinases = new HashSet<Composite> ();
				this.residueMap.put(residue, kinases);
			}
			// Either way add the current kinase to the set (now)
			// associated with the residue
			kinases.add(kinase);
		}
		
		/*
		 * When we actually create the action for attaching a kinase
		 * we need to make a new protein which has the kinase already
		 * attached.
		 */
		@SuppressWarnings("unchecked")
		public Protein attachKinaseAtResidue(Composite kinase, String residue){
			Composite newProteinName = this.composite.combine(kinase);
			Protein newProtein = new Protein (newProteinName);
			
			// We copy across the current protein's residue map to the
			// newly formed composite of the protein plus the attached
			// kinase. However we not cannot attach anything else at the
			// same residue hence we remove the residue from the cloned
			// residuemap.
			newProtein.residueMap = 
				(HashMap<String, HashSet<Composite>>) residueMap.clone ();
			newProtein.residueMap.remove(residue);
			
			return newProtein;
		}
	}
	
	
	private class Composite extends Object {
		HashSet<String> constituents;
		
		Composite (){
			this.constituents = new HashSet<String> ();
		}
		
		Composite(String component){
			this.constituents = new HashSet<String> ();
			this.constituents.add(component);
		}
		
		public Composite combine (Composite additional){
			Composite result = new Composite();
			result.constituents.addAll(this.constituents);
			result.constituents.addAll(additional.constituents);
			
			return result;
		}
		
		public String printSyntax (){
		    String[] names = 
		    	constituents.toArray(new String [this.constituents.size()]);
		    if (names.length == 0){
		    	return "UnknownConstituents";
		    }
		    String name = names[0];
			for (int index = 1; index < names.length; index++){
				name = name + ":" + names[index];
			}
			
			return name;
		}
		
		public boolean equals(Object obj){
			if (!(obj instanceof Composite)){
				return false;
			}
			Composite c = (Composite) obj;
			boolean result = this.constituents.equals(c.constituents);
			return result;
		}
		
		public int hashCode (){
			return this.constituents.hashCode();
		}
	}
	
	private class Reaction {
		HashSet<Composite> reactants;
		HashSet<Composite> products;
		
		String name;
		
		Reaction (String name,
				HashSet<Composite> reactants,
				HashSet<Composite> products){
			this.name = name;
			this.reactants = reactants;
			this.products = products;
		}
		
		public String getName (){
			return this.name;
		}
		public Set<Composite> getReactants(){
			return this.reactants;
		}
		public Set<Composite> getProducts(){
			return this.products;
		}
		
		public String printSyntax (){
			LineStringBuilder sb = new LineStringBuilder ();
			
			sb.append (this.name);
			sb.append (": ");
			
			boolean first = true;
			for (Composite reactant : this.reactants){
				if (!first){
					sb.append(" + ");
				}
				first = false;
				
				sb.append(reactant.printSyntax());
			}
			
			sb.append(" ----> ");
			
			first = true;
			for (Composite product : this.products){
				if (!first){
					sb.append(" + ");
				}
				first = false;
				
				sb.append(product.printSyntax());
			}
			
			return sb.toString();
		}
	}
	
	private class BioPEPAComponent {
		Composite name;
		HashSet<String> reactantReactions;
		HashSet<String> productReactions;
		BioPEPAComponent (Composite name){
			this.name = name;
			this.reactantReactions = new HashSet<String>();
			this.productReactions = new HashSet<String>();
		}
		
		public void addReactantReaction (String name){
			this.reactantReactions.add(name);
		}
		public void addProductReaction (String name){
			this.productReactions.add(name);
		}
		
		public String printSyntax (){
			String result = name.printSyntax() + " = ";
			
			for (String r : this.reactantReactions){
				result = result + " " + r + " << ";
			}
			for (String r : this.productReactions){
				result = result + " " + r + " >> ";
			}
			
			return result + " ;";
		}
	}

}
