package uk.ac.ed.inf.biopepa.core.analysis;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ed.inf.biopepa.core.compiler.CompiledDynamicComponent;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpression;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpressionVisitor;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledFunction;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledNumber;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledOperatorNode;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledSystemVariable;

public class ReactantsParticipantsVisitor extends CompiledExpressionVisitor {

	private boolean reactantsInvolved;
	private HashSet<String> externalParticipants;
	public ReactantsParticipantsVisitor (){
		this.externalParticipants = new HashSet<String> ();
		this.reactantsInvolved = false;
	}
	
	public Set<String> getExternalParticipants(){
		return this.externalParticipants;
	}
	
	/*
	 * Returns whether or not the reactants are involved
	 * via a predefined rate law function. Hence one can
	 * check if a *reactant* is involved in the rate by
	 * checking whether it is explicitly mentioned
	 * via 'getExternalParticipants' or whether a ratelaw
	 * is used.
	 */
	public boolean getReactantsInvolved(){
		return this.reactantsInvolved;
	}
	
	@Override
	public boolean visit(CompiledDynamicComponent component) {
		externalParticipants.add(component.getName());
		return false;
	}

	@Override
	public boolean visit(CompiledFunction function) {
		if (function.getFunction().isRateLaw()){
			this.reactantsInvolved = true;
		}
		for (CompiledExpression ce : function.getArguments()){
			ce.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(CompiledNumber number) {
		return false;
	}

	@Override
	public boolean visit(CompiledOperatorNode operator) {
		/*
		 * We'd prefer to do the simple thing of just
		 * descending into both expressions however either
		 * may be null. If indeed they are null then it is
		 * likely due to an error which should be detected
		 * earlier.
		 */
		CompiledExpression left  = operator.getLeft();
		CompiledExpression right = operator.getRight();
		if (left != null){
			left.accept(this);
		}
		if (right != null){
			right.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(CompiledSystemVariable variable) {
		return false;
	}

}
