package uk.ac.ed.inf.biopepa.core.compiler;

import java.util.Map;

/*
 * This class is intended to be a simple evaluator for
 * compiled expressions which will work for evaluating
 * dynamic components.
 * NOTE: it is incomplete in the sense that if the compiled
 * expression is a kinetic law such as fMA(r) then this will
 * fail as there is no context (ie. the reaction) in which to
 * evaluate this rate function. However this class can be used
 * to retrospectively add dynamic variables to results. It could
 * also be extended to allow for rate based expressions.
 * (see CompiledExpressionRateEvaluator).
 */
public class CompiledExpressionEvaluator extends CompiledExpressionVisitor {

	Map<String, Number> componentCounts;
	double currentTime;
	DynamicExpressionModelContext modelContext;
	public CompiledExpressionEvaluator (DynamicExpressionModelContext model,
			Map<String, Number> componentCounts,
			double time){
		this.componentCounts = componentCounts;
		this.modelContext = model;
		this.currentTime = time;
	}
	
	protected double result;
	public double getResult(){
		return this.result;
	}
	
	@Override
	public boolean visit(CompiledDynamicComponent component) {
		String name = component.getName();
		if (modelContext.containsComponent(name)){
			Number count = componentCounts.get(name);
			this.result = count.doubleValue();
			return false;
		} else if (modelContext.containsVariable(name)){
			/*
			 * We should be careful to avoid an infinite
			 * loop here, if the definition is (mutually) recursive
			 * then basically we're in trouble.
			 */
			CompiledExpression varExp = modelContext.getDynamicExpression(name);
			varExp.accept(this);
			return false;
		} else {
			throw new IllegalStateException ();
		}
	}

	@Override
	public boolean visit(CompiledFunction function) {
		if (function.getFunction().isRateLaw()) {
			/*
			 * The rate kinetic functions cannot be evaluated within
			 * this context because there is no surrounding reaction
			 * with which to evaulate the function. For example fMA
			 * requires the reactants of the reaction. If evaluating
			 * a reaction rate we should use a subclass of this one
			 * which takes in the reaction in the constructor and
			 * can override this method with one which evaluates the
			 * the rate laws properly.
			 */
			throw new IllegalStateException ();
		} else {
			// If it is not a rate law then we can attempt to
			// interpret it as a normal maths function.
			if (function.getFunction().args() == 1) {
				function.getArguments().get(0).accept(this);
				double argument = result ;
				switch (function.getFunction()) {
				case LOG:
					result = Math.log(argument);
					break;
				case EXP:
					result = Math.exp(argument);
					break;
				case H:
					result = (argument > 0) ? 1 : 0;
					break;
				case FLOOR:
					result = Math.floor(argument);
					break;
				case CEILING:
					result = Math.ceil(argument);
					break;
				case TANH:
					result = Math.tanh(argument);
					break;
				default:
					throw new IllegalStateException();
				}
			} else {
				throw new IllegalStateException ();
			}
			return false;
		}
	}

	@Override
	public boolean visit(CompiledNumber number) {
		result = number.getNumber().doubleValue();
		return false;
	}

	@Override
	public boolean visit(CompiledOperatorNode operator) {
		operator.getLeft().accept(this);
		double left = result ;
		operator.getRight().accept(this);
		double right = result ;
		switch (operator.getOperator()) {
		case PLUS:
			result = left + right;
			break;
		case MINUS:
			result = left - right;
			break;
		case DIVIDE:
			result = left / right;
			break;
		case MULTIPLY:
			result = left * right;
			break;
		case POWER:
			result = Math.pow(left, right);
			break;
		default:
			throw new IllegalStateException ();
		}
		return false;
	}

	@Override
	public boolean visit(CompiledSystemVariable variable) {
		switch (variable.getVariable()) {
		case TIME:
			result = currentTime ;
			break;
		default:
			throw new IllegalStateException();
		}
		return false;
	}

}
