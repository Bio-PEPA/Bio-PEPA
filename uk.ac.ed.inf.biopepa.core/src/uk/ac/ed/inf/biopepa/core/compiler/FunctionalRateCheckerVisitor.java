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
import uk.ac.ed.inf.biopepa.core.dom.InfixExpression;
import uk.ac.ed.inf.biopepa.core.dom.Name;
import uk.ac.ed.inf.biopepa.core.dom.NumberLiteral;

/**
 * @author Mirco
 * 
 */
public class FunctionalRateCheckerVisitor extends ExpressionEvaluatorVisitor {

	private boolean top = true;
	boolean predefinedLaw = false;

	public FunctionalRateCheckerVisitor(ModelCompiler compiler) {
		super(compiler);
	}

	@Override
	public boolean visit(Name name) throws BioPEPAException {
		top = false;
		return super.visit(name);
	}

	@Override
	public boolean visit(InfixExpression infixExpression) throws BioPEPAException {
		top = false;
		return super.visit(infixExpression);
	}

	@Override
	public boolean visit(NumberLiteral numberLiteral) throws BioPEPAException {
		top = false;
		return super.visit(numberLiteral);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.FunctionCall)
	 */
	@Override
	public boolean visit(FunctionCall functionCall) throws BioPEPAException {
		/*if (top) {*/
			top = false;
			Function f = CompiledFunction.checkFunction(compiler, functionCall);
			if (f.isRateLaw()) {
				predefinedLaw = true;
				CompiledFunction efn = new CompiledFunction();
				efn.setFunction(f);
				int i = 0;
				ExpressionEvaluatorVisitor eev;
				for (Expression e : functionCall.arguments()) {
					eev = new ExpressionEvaluatorVisitor(compiler);
					e.accept(eev);
					efn.setArgument(i++, eev.getExpressionNode());
					node = efn;
				}
				return true;
			}
		/*}*/
		return super.visit(functionCall);
	}

}
