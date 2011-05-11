package uk.ac.ed.inf.biopepa.core.compiler;

/*
 * When simulating models we may require the
 * evaluation of a dynamic variable's expression.
 * We may be in the context of a compiled model
 * or we may even be in the context of an SBAModel
 * either way we have a shared interface such that
 * both can represent the model context in which to
 * evaluate the dynamic expression.
 */

public interface DynamicExpressionModelContext {
	/*
	 * Returns true if the model contains a component
	 * with the given name.
	 */
	public boolean containsComponent(String name);
	
	/*
	 * Returns true if the model contains a dynamic
	 * variable with the given name.
	 */
	public boolean containsVariable(String name);
	
	/*
	 * Returns the compiled expression relating to
	 * the given dynamic variable name
	 */
	public CompiledExpression getDynamicExpression (String name);
	
}
