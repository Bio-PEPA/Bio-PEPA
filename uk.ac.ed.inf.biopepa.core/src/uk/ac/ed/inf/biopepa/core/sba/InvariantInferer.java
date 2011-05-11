package uk.ac.ed.inf.biopepa.core.sba;

import java.util.LinkedList;

import uk.ac.ed.inf.biopepa.core.Utilities;
import uk.ac.ed.inf.biopepa.core.analysis.IntegerMatrix;
import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour.Type;

public class InvariantInferer {
	// private SBAModel sbaModel;
	// private CompartmentData[] compartments;
	private ComponentNode[] species;
	private SBAReaction[] reactions;

	/*
	 * The given list of reactions are those to include in the
	 * analysis, which when using the wizard can be less than
	 * the full set of reactions in the model. If you wish to use
	 * the full set of reactions in the model then given the parameter
	 * as null, will mean we will select all of the reactions in the model.
	 */
	public InvariantInferer(SBAModel sbaModel, 
							LinkedList<SBAReaction>reactions) {
		this.species = sbaModel.getComponents();
		
		if (reactions == null){
		  this.reactions = sbaModel.getReactions();
		} else {
		  this.reactions = reactions.toArray(new SBAReaction[0]);
		}
	}	
	
	private IntegerMatrix modelMatrix;

	public void computeModelMatrix() {
		int rows = species.length;
		int columns = reactions.length;

		IntegerMatrix imatrix = new IntegerMatrix(rows, columns);

		// The rows represent the species
		for (int rowNumber = 0; rowNumber < rows; rowNumber++) {
			ComponentNode comp = species[rowNumber];
			String compName = comp.getName();

			// The columns represent the reactions
			// So the value at (rowNumber, colNumber) is set to
			// effect which the given reaction has on the given component
			// which may be negative if the component is a reactant.
			for (int colNumber = 0; colNumber < columns; colNumber++) {
				SBAReaction reaction = reactions[colNumber];
				int effectOnComp = 0;
				// First if it is a reactant then it will be decreased
				// by the reaction
				for (SBAComponentBehaviour cb : reaction.getReactants()) {
					if (cb.getType().equals(Type.REACTANT) && cb.getName().equals(compName)) {
						effectOnComp -= cb.getStoichiometry();
					}
				}
				// Next if it is a product then it is increased by the
				// reaction.
				for (SBAComponentBehaviour cb : reaction.getProducts()) {
					if (cb.getName().equals(compName)) {
						effectOnComp += cb.getStoichiometry();
					}
				}
				imatrix.set(rowNumber, colNumber, effectOnComp);

			}
		}
		modelMatrix = imatrix;
	}
	
	/* 
	 * Obviously should not be called before 'computeModelMatrix()'
	 */
	public IntegerMatrix getModelMatrix(){
		return modelMatrix;
	}

	private IntegerMatrix stateInvariantSolution = null;

	private void computeStateInvariantSolution() {
		if (modelMatrix == null) {
			throw new IllegalStateException();
		}
		IntegerMatrix solvedMatrix = modelMatrix.solveFourierMotzkin();
		stateInvariantSolution = solvedMatrix;
	}
	
	public IntegerMatrix getStateInvariantSolution(){
		if (stateInvariantSolution == null){
			computeStateInvariantSolution();
		}
		return stateInvariantSolution;
	}
	
	public LinkedList<String> sumOfAllStateInvariants(){
		IntegerMatrix invariantMatrix = getStateInvariantSolution();
		// Our sum total invariant will have the same length as any
		// row in the matrix.
		int numColumns = invariantMatrix.getColumnDimension();
		int [] sumInvariant = new int[numColumns];
		for (int colIndex = 0; colIndex < numColumns; colIndex++){
			sumInvariant[colIndex] = 0;
		}
			
		// For each row in the matrix we simply add it to the current
		for (int rowIndex = 0 ; 
		 	 rowIndex < invariantMatrix.getRowDimension(); 
		 	 rowIndex++){
			for (int colIndex = 0; colIndex < numColumns; colIndex++){
				int value = invariantMatrix.get(rowIndex, colIndex);
				sumInvariant[colIndex] += value;
			}
		}
		LinkedList<String> result = new LinkedList<String>();
		for (int colIndex = 0; colIndex < numColumns; colIndex++){
			ComponentNode comp = species[colIndex];
			String compName = comp.getName();
			int value = sumInvariant[colIndex];
			result.addLast(value + " x " + compName);
		}
		return result;
	}

	public SimpleTree getStateInvariantTree(){
		IntegerMatrix invariantMatrix = getStateInvariantSolution();
		SimpleTree stateInvTree = new SimpleTree ("State Invariants");
		
		/*
		 * The returned invariant matrix has a row for each
		 * invariant. The columns in each row are in the order
		 * of the modelMatrix's rows, so there is one for each
		 * species with the value indicating how that species is
		 * involved in the invariant.
		 */
		
		for (int rowIndex = 0 ; 
				 rowIndex < invariantMatrix.getRowDimension(); 
				 rowIndex++){
			String thisName = "State invariant " + (rowIndex + 1);
			SimpleTree thisInvTree = stateInvTree.addNamedChild(thisName);
			
			for (int colIndex = 0; 
			         colIndex < invariantMatrix.getColumnDimension(); 
			         colIndex++){
				
				int value = invariantMatrix.get(rowIndex, colIndex);
				ComponentNode comp = species[colIndex];
				String compName = comp.getName();
				switch (value){
				case 0: break;
				case 1:  thisInvTree.addNamedChild(compName); break;
				case -1: thisInvTree.addNamedChild("-" + compName); break;
				default: thisInvTree.addNamedChild("(" + value + 
						                           " * " + compName + ")") ;
				}
			}						
		}
		
		if (!stateInvTree.hasChildren()){
			String name = "There are no invariants in this model";
			stateInvTree.addNamedChild(name);
		}
		
		return stateInvTree;
	}
	
	public SimpleTree getUncoveredStateTree(){
		IntegerMatrix invariantMatrix = getStateInvariantSolution();
		SimpleTree uncoveredTree = new SimpleTree ("Uncovered Species:");

		
		// Each column represents a single component
		for(int colIndex = 0; 
				colIndex < invariantMatrix.getColumnDimension(); 
				colIndex++){
			
			// So for each column/component we check if any row is nonzero
			// if so then it is involved in an invariant 
			boolean seen = false;
			for (int rowIndex = 0; rowIndex < invariantMatrix.getRowDimension(); rowIndex++){
				if (invariantMatrix.get(rowIndex, colIndex) != 0){
					seen = true ;
					break;
				}
			}
	        // If we get here without setting 'seen' then the component is not involved
			// in any invariant
			if (!seen){
				String childName = species[colIndex].getName();
				uncoveredTree.addNamedChild(childName);
			}
		}
		
		if (!uncoveredTree.hasChildren()){
			String name = "There are no uncovered species in this model";
			uncoveredTree.addNamedChild(name);
		}
		
		return uncoveredTree;
	}
	
	
	
	public String printStateInvariantSolution(){
		LineStringBuilder lsb = new LineStringBuilder();
		for (String line : getStateInvariantStrings()){
			lsb.appendLine(line);
		}
		return lsb.toString();
	}
	
	public LinkedList<String> getStateInvariantStrings(){
		LinkedList<String> resultStrings = new LinkedList<String>();
		IntegerMatrix invariantMatrix = getStateInvariantSolution();
		/*
		 * The returned invariant matrix has a row for each
		 * invariant. The columns in each row are in the order
		 * of the modelMatrix's rows, so there is one for each
		 * species with the value indicating how that species is
		 * involved in the invariant.
		 */
		for (int rowIndex = 0 ; rowIndex < invariantMatrix.getRowDimension(); rowIndex++){
			StringBuilder sb = new StringBuilder();
			// Build up a list of terms so that we can add the '+' signs in between.
			LinkedList <String> terms = new LinkedList<String> ();
			for (int colIndex = 0; colIndex < invariantMatrix.getColumnDimension(); colIndex++){
				int value = invariantMatrix.get(rowIndex, colIndex);
				ComponentNode comp = species[colIndex];
				String compName = comp.getName();
				switch (value){
				case 0: break;
				case 1: terms.add(compName); break;
				case -1: terms.add("-" + compName); break;
				default: terms.add("(" + value + " * " + compName + ")") ;
				}
			}
			
			switch (terms.size()){
			case 0: sb.append("no terms"); break;
			case 1: sb.append(terms.getFirst()); break;
			default: sb.append(Utilities.intercalateStrings(terms, " + "));
			}
			
			sb.append(" is an invariant in this model");
			resultStrings.addLast(sb.toString());
		}
		
		return resultStrings;
	}
	
	public LinkedList<String> getUncoveredStateStrings(){
		LinkedList<String> resultStrings = new LinkedList<String>();
		IntegerMatrix invariantMatrix = getStateInvariantSolution();
		
		// Each column represents a single component
		for(int colIndex = 0; colIndex < invariantMatrix.getColumnDimension(); colIndex++){
			// So for each column/component we check if any row is nonzero
			// if so then it is involved in an invariant 
			boolean seen = false;
			for (int rowIndex = 0; rowIndex < invariantMatrix.getRowDimension(); rowIndex++){
				if (invariantMatrix.get(rowIndex, colIndex) != 0){
					seen = true ;
					break;
				}
			}
	        // If we get here without setting seen then the component is not involved
			// in any invariant
			if (!seen){
				resultStrings.add(species[colIndex].getName());
			}
		}
		
		return resultStrings;
	}
	
	/* Now the activity invariant matrix is got by solving the transpose
	 * of the model matrix.
	 */
	private IntegerMatrix activityInvariantSolution = null;

	private void computeActivityInvariantSolution() {
		if (modelMatrix == null) {
			throw new IllegalStateException();
		}
		IntegerMatrix solvedMatrix = modelMatrix.transpose().solveFourierMotzkin();
		activityInvariantSolution = solvedMatrix;
	}
	
	public IntegerMatrix getActivityInvariantSolution(){
		if (activityInvariantSolution == null){
			computeActivityInvariantSolution();
		}
		return activityInvariantSolution;
	}

	public String printActivityInvariantSolution(){
		LineStringBuilder lsb = new LineStringBuilder();
		for (String line : getActivityInvariantStrings()){
			lsb.appendLine(line);
		}
		return lsb.toString();
	}
	
	public SimpleTree getActivityInvariantTree(){
		IntegerMatrix invariantMatrix = getActivityInvariantSolution();
		SimpleTree actInvTree = new SimpleTree("Activity Invariants:");
		/*
		 * The returned invariant matrix has a row for each
		 * invariant. The columns in each row are in the order
		 * of the modelMatrix's rows, so there is one for each
		 * species with the value indicating how that species is
		 * involved in the invariant.
		 */
		
		for (int rowIndex = 0 ; 
				 rowIndex < invariantMatrix.getRowDimension(); 
				 rowIndex++){
			String thisName = "reaction loop " + (rowIndex + 0);
			SimpleTree thisInv = actInvTree.addNamedChild(thisName);
			for (int colIndex = 0; colIndex < invariantMatrix.getColumnDimension(); colIndex++){
				int value = invariantMatrix.get(rowIndex, colIndex);
				SBAReaction reaction = reactions[colIndex];
				String reactName = reaction.getName();
				switch (value){
				case 0: break;
				case 1:  thisInv.addNamedChild(reactName); break;
				case -1: thisInv.addNamedChild("-" + reactName); break;
				default: thisInv.addNamedChild("(" + value + 
						                          " * " + reactName + ")") ;
				}
			}
		}
			
		if (!actInvTree.hasChildren()){
			actInvTree.addNamedChild("No reaction loops found");
		}
		
		return actInvTree;
	}
	
	public SimpleTree getUncoveredActivityTree(){
		IntegerMatrix invariantMatrix = getActivityInvariantSolution();
		SimpleTree uncoveredTree = new SimpleTree ("Uncovered reactions:");
		
		// Each column represents a single reaction
		for(int colIndex = 0; 
		        colIndex < invariantMatrix.getColumnDimension();
		        colIndex++){
			// So for each column/reaction we check if any row is nonzero
			// if so then it is involved in an invariant/loop 
			boolean seen = false;
			for (int rowIndex = 0; rowIndex < invariantMatrix.getRowDimension(); rowIndex++){
				if (invariantMatrix.get(rowIndex, colIndex) != 0){
					seen = true ;
					break;
				}
			}
	        // If we get here without setting seen then the component is not involved
			// in any invariant
			if (!seen){
				String childName = reactions[colIndex].getName();
				uncoveredTree.addNamedChild(childName);
			}
		}
		
		if (!uncoveredTree.hasChildren()){
			String name = "There are no uncovered reactions in this model";
			uncoveredTree.addNamedChild(name);
		}
		
		return uncoveredTree;
	}
	
	public LinkedList<String> getActivityInvariantStrings(){
		LinkedList<String> resultStrings = new LinkedList<String>();
		IntegerMatrix invariantMatrix = getActivityInvariantSolution();
		/*
		 * The returned invariant matrix has a row for each
		 * invariant. The columns in each row are in the order
		 * of the modelMatrix's rows, so there is one for each
		 * species with the value indicating how that species is
		 * involved in the invariant.
		 */
		
		for (int rowIndex = 0 ; rowIndex < invariantMatrix.getRowDimension(); rowIndex++){
			StringBuilder sb = new StringBuilder();
			// Build up a list of terms so that we can add the '+' signs in between.
			LinkedList <String> terms = new LinkedList<String> ();
			for (int colIndex = 0; colIndex < invariantMatrix.getColumnDimension(); colIndex++){
				int value = invariantMatrix.get(rowIndex, colIndex);
				SBAReaction reaction = reactions[colIndex];
				String reactName = reaction.getName();
				switch (value){
				case 0: break;
				case 1: terms.add(reactName); break;
				case -1: terms.add("-" + reactName); break;
				default: terms.add("(" + value + " * " + reactName + ")") ;
				}
			}
			
			switch (terms.size()){
			case 0: sb.append("no terms"); break;
			case 1: sb.append("The reaction: " + terms.getFirst() + 
					" has no effect on the model"); break;
			default:
				// Note  -1 so that we do not take the final term
				sb.append("Performing the following reactions returns "+
						"the model to the same state: ");
				sb.append(Utilities.intercalateStrings(terms, " + "));
			}
			resultStrings.addLast(sb.toString());
		}
		
		return resultStrings;
	}
	
	public LinkedList<String> getUncoveredActivityStrings(){
		LinkedList<String> resultStrings = new LinkedList<String>();
		IntegerMatrix invariantMatrix = getActivityInvariantSolution();
		
		// Each column represents a single reaction
		for(int colIndex = 0; colIndex < invariantMatrix.getColumnDimension(); colIndex++){
			// So for each column/reaction we check if any row is nonzero
			// if so then it is involved in an invariant/loop 
			boolean seen = false;
			for (int rowIndex = 0; rowIndex < invariantMatrix.getRowDimension(); rowIndex++){
				if (invariantMatrix.get(rowIndex, colIndex) != 0){
					seen = true ;
					break;
				}
			}
	        // If we get here without setting seen then the component is not involved
			// in any invariant
			if (!seen){
				resultStrings.add(reactions[colIndex].getName());
			}
		}
		
		return resultStrings;
	}

}
