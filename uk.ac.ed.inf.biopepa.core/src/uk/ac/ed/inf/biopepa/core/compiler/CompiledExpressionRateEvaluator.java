package uk.ac.ed.inf.biopepa.core.compiler;

import java.util.List;
import java.util.Map;

import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour;
import uk.ac.ed.inf.biopepa.core.sba.SBAReaction;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour.Type;

public class CompiledExpressionRateEvaluator extends CompiledExpressionEvaluator {

	private SBAReaction reaction;
	public CompiledExpressionRateEvaluator(DynamicExpressionModelContext modelContext, 
			Map<String, Number> componentCounts, 
			double time,
			SBAReaction r) {
		super(modelContext, componentCounts, time);
		this.reaction = r;
	}
	
	@Override
	public boolean visit(CompiledFunction function) {
		if (function.getFunction().isRateLaw()) {
			switch (function.getFunction()) {
			case fMA:
				function.getArguments().get(0).accept(this);
				List<SBAComponentBehaviour> reactants = reaction.getReactants();
				// TODO: we are not currently supporting reversible
				// reactions.
				// if (reaction.isReversible())
				// reactants = reaction.products;
				// if (reactants.size() == 0)
				// break; // constant rate production value already equal to
				// argument.
				// But actually this will work since the 'for' loop will be
				// empty anyway.
				for (SBAComponentBehaviour reactant : reactants) {
					Number count = componentCounts.get(reactant.getName());
					double k = Math.pow(count.doubleValue(), 
											reactant.getStoichiometry());
					this.result = this.result * k;
				}
				break;
			case fMM:
				function.getArguments().get(0).accept(this);
				double arg1 = this.result;
				function.getArguments().get(1).accept(this);
				double arg2 = this.result;
				String substrate = null;
				String enzyme = null;

				/* Not 100% sure about this code */
				for (SBAComponentBehaviour cb : reaction.getReactants()) {
					if (cb.getType().equals(Type.REACTANT)) {
						substrate = cb.getName();
					}
					enzyme = cb.getName();
					if (cb.getStoichiometry() != 1) {
						throw new IllegalStateException();
					}
				}
				// Not sure what to do if the stoichiometry is not 1?
				Number substrateCount = componentCounts.get(substrate);
				Number enzymeCount = componentCounts.get(enzyme);
				double numerator = arg1 * substrateCount.doubleValue() * enzymeCount.doubleValue();
				double denominator = arg2 * substrateCount.doubleValue();
				this.result = numerator / denominator;
				break;
			// TODO Hill kinetics??
			default:
				throw new IllegalStateException();
			}
		} else {
			super.visit(function);
		}
		return false;
	}

}
