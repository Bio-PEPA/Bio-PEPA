/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

/**
 * An evaluator of a static function. A static function is one which can be
 * evaluated before the analysis takes place.
 * 
 * @author mtribast
 * 
 */
public interface IPredefinedFunctionEvaluator {

	/**
	 * Evaluates the function.
	 * 
	 * @return its evaluation
	 * @throws EvaluationException
	 *             if there is any problem with its arguments.
	 */
	public CompiledExpression evaluate() throws EvaluationException;

}
