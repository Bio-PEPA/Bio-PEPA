/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledFunction.Function;
import uk.ac.ed.inf.biopepa.core.dom.Expression;
import uk.ac.ed.inf.biopepa.core.dom.FunctionCall;

public class FunctionEvaluators {

	public static class GenericOneArgumentFunction implements IPredefinedFunctionEvaluator {

		private ModelCompiler compiler;

		private FunctionCall call;

		private Function function;

		public GenericOneArgumentFunction(ModelCompiler compiler, FunctionCall call) throws CompilerException {
			this.compiler = compiler;
			this.call = call;
			function = CompiledFunction.checkFunction(compiler, call);
		}

		public CompiledExpression evaluate() throws EvaluationException {
			Expression e = call.arguments().get(0);
			ExpressionEvaluatorVisitor v = new ExpressionEvaluatorVisitor(compiler);
			try {
				e.accept(v);
			} catch (BioPEPAException e1) {
				throw new EvaluationException();
			}
			
			// A literal translation of the function into a
			// compiled function. This will serve as the return
			// result if the whole function call is dynamic (such as fMA(r))
			// or the expanded form if the whole call can be statically
			// evaluated, such as floor (r1 * r2) where r1 and r2 are static.
			CompiledFunction efn = new CompiledFunction();
			efn.setFunction(function);
			efn.setArgument(0, v.getExpressionNode());
			
			if (v.getExpressionNode() instanceof CompiledNumber) {
				CompiledNumber number = null;
				double d = ((CompiledNumber) v.getExpressionNode()).doubleValue();
				switch (function) {
				case H:
					number = new CompiledNumber((d > 0.00 ? new Long(1) : new Long(0)));
					break;
				case FLOOR:
					number = new CompiledNumber(((long) Math.floor(d)));
					break;
				case CEILING:
					number = new CompiledNumber(((long) Math.ceil(d)));
					break;
				case LOG:
					number = new CompiledNumber(Math.log(d));
					break;
				case EXP:
					number = new CompiledNumber(Math.exp(d));
					break;
				case TANH:
					number = new CompiledNumber(Math.tanh(d));
					break;
				default:
					System.err.println("shouldn't get here?");
				}
				number.setExpandedForm(efn);
				return number;
			}
			
			return efn;
		}
	}
}
