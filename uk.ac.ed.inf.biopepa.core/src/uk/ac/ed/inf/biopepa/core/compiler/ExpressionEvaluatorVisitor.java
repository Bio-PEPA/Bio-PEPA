/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledFunction.Function;
import uk.ac.ed.inf.biopepa.core.compiler.ProblemInfo.Severity;
import uk.ac.ed.inf.biopepa.core.dom.*;

public class ExpressionEvaluatorVisitor extends DefaultCompilerVisitor {

	protected CompiledExpression node = null;

	public ExpressionEvaluatorVisitor(ModelCompiler compiler) {
		super(compiler);
	}

	public CompiledExpression getExpressionNode() {
		return node;
	}

	@Override
	public boolean visit(Name name) throws BioPEPAException {
		String identifier = name.getIdentifier();
		if (identifier == null)
			throw new IllegalArgumentException();
		if (identifier == compiler.getCurrentlyVisitedVariable()) {
			compiler.problemRequestor.accept(ProblemKind.CIRCULAR_USAGE, name);
			throw new CompilerException();
		}
		
		/* Check if the variable is dynamic in which case it cannot be
		 * compiled.
		 */
		if (compiler.isDynamic(identifier)) {
			// System.out.println ("The identifier: " + identifier + " is dynamic");
			node = new CompiledDynamicComponent(identifier);
			return true;
		} else {
			VariableData data = compiler.checkAndGetVariableData(identifier);
			if (data == null) {
				// variable not yet defined
				ProblemKind pKind = ProblemKind.VARIABLE_USED_BUT_NOT_DEFINED;
				pKind.setMessage("Variable: " + identifier +
						         " used but not defined");
				compiler.problemRequestor.accept(pKind, name);
				throw new CompilerException();
			}
			/*
			 * So if the variable is not dynamic then we still set the expanded form
			 * to CompiledDynamicComponent.
			 */
			CompiledExpression nodeExp = data.getValue();
			if (nodeExp != null){
				node = data.getValue().clone();
				node.setExpandedForm(new CompiledDynamicComponent(identifier));
			} else {
				// It's unclear what to do here, this probably means that there
				// was an error in the definition of this variable, so that the
				// variable itself has been defined but with an expression
				// which itself contains an error. eg if 'b' is undefined then
				// a = b + 1;
				// c = a;
				// We'll get here when evaluating the expression in the def of
				// 'c', since 'a' is defined but by an errorful expression.
				// I think the best thing to do here is nothing since we should
				// have caught the original error and will report it there and
				// the current 'error' will be automatically fixed.
				// System.out.println ("Sheesh, nodeExp is null for: " + identifier);
			}
			return true;
		}
	}

	@Override
	public boolean visit(InfixExpression infixExpression) throws BioPEPAException {
		ExpressionEvaluatorVisitor vlhs = new ExpressionEvaluatorVisitor(compiler);
		infixExpression.getLeftHandSide().accept(vlhs);
		ExpressionEvaluatorVisitor vrhs = new ExpressionEvaluatorVisitor(compiler);
		infixExpression.getRightHandSide().accept(vrhs);
		if (vlhs.node instanceof CompiledNumber && vrhs.node instanceof CompiledNumber) {
			Number number;
			CompiledNumber lenn = ((CompiledNumber) vlhs.node);
			CompiledNumber renn = ((CompiledNumber) vrhs.node);
			switch (infixExpression.getOperator()) {
			case PLUS:
				if (lenn.evaluatesToDouble() || renn.evaluatesToDouble())
					number = new Double(lenn.doubleValue() + renn.doubleValue());
				else
					number = new Long(lenn.longValue() + renn.longValue());
				break;
			case MINUS:
				if (lenn.evaluatesToDouble() || renn.evaluatesToDouble())
					number = new Double(lenn.doubleValue() - renn.doubleValue());
				else
					number = new Long(lenn.longValue() - renn.longValue());
				break;
			case TIMES:
				if (lenn.evaluatesToDouble() || renn.evaluatesToDouble())
					number = new Double(lenn.doubleValue() * renn.doubleValue());
				else
					number = new Long(lenn.longValue() * renn.longValue());
				break;
			case DIVIDE:
				if (renn.doubleValue() == 0.00) {
					compiler.problemRequestor.accept(Severity.ERROR, "Divide by zero.", infixExpression);
					throw new CompilerException();
				}
				if (lenn.evaluatesToLong() && renn.evaluatesToLong()) {
					long l = lenn.longValue(), r = renn.longValue();
					long a = l / r;
					if (a * r == l) {
						number = new Long(a);
						break;
					}
				}
				number = new Double(lenn.doubleValue() / renn.doubleValue());
				break;
			case POWER:
				double d = Math.pow(lenn.doubleValue(), renn.doubleValue());
				if (Double.isNaN(d) || Double.isInfinite(d)) {
					compiler.problemRequestor
							.accept(Severity.ERROR, "Cannot evaluate the expression.", infixExpression);
					throw new CompilerException();
				}
				if ((d - ((long) d)) != 0.00)
					number = new Double(d);
				else
					number = new Long(((long) d));
				break;
			default:
				compiler.problemRequestor.accept(ProblemKind.INVALID_OPERATOR_FOR_DOUBLE, infixExpression);
				throw new CompilerException();
			}
			node = new CompiledNumber(number);
			// Set expandedForm if required
			if (lenn.hasExpandedForm() || renn.hasExpandedForm()) {
				CompiledOperatorNode eNode = new CompiledOperatorNode();
				eNode.setOperator(infixExpression.getOperator());
				if (lenn.hasExpandedForm())
					eNode.left = lenn.expandedForm;
				else
					eNode.left = lenn;
				if (renn.hasExpandedForm())
					eNode.right = renn.expandedForm;
				else
					eNode.right = renn;
				node.setExpandedForm(eNode);
			}
			return true;
		}
		CompiledOperatorNode tnode = new CompiledOperatorNode();
		tnode.setOperator(infixExpression.getOperator());
		tnode.setLeft(vlhs.node);
		tnode.setRight(vrhs.node);
		node = tnode;
		return true;
	}

	@Override
	public boolean visit(NumberLiteral numberLiteral) throws BioPEPAException {
		Number number;
		try {
			number = Long.parseLong(numberLiteral.getToken());
		} catch (NumberFormatException e1) {
			try {
				number = Double.parseDouble(numberLiteral.getToken());
			} catch (NumberFormatException e2) {
				compiler.problemRequestor.accept(ProblemKind.INVALID_NUMBER_LITERAL, numberLiteral);
				throw new CompilerException();
			}
		}
		node = new CompiledNumber(number);
		return true;
	}

	@Override
	public boolean visit(FunctionCall functionCall) throws BioPEPAException {
		// The code contained within the 'if' statement here was added by
		// me (allan) in order to potentially allow rate functions with
		// predefined rate laws contained within an expression, ie. that are
		// not at the top-level. We were certainly handling these incorrectly
		// we should at least add a problem saying  you're not allowed to
		// have these at the non-top level. So for example if you write
		// r = [ 1 + fMA(a) ]
		// Then before this code was added you would silently get an error
		// when the function was attempted to be evaluated. Now you at least
		// do not get that error, however I'm not sure that all is well,
		// what happens if you write D[fMA(r)] in the system equation?
		Function f = CompiledFunction.checkFunction(compiler, functionCall);
		if (f.isRateLaw()) {
			// Hmm, not sure about this, basically I'm worried that if I
			// write r = [ 1 + fMA(a) ]
			// then I'll get a warning that the reaction rate does not depend
			// on its Reactants.
			// predefinedLaw = true;
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
		
		
		IPredefinedFunctionEvaluator evaluator;
		evaluator = CompiledFunction.getFunctionEvaluator(compiler, functionCall);
		try {
			node = evaluator.evaluate();
		} catch (EvaluationException e) {
			// Already handled by the code throwing the EvaluationException
		}
		return true;
	}

	@Override
	public boolean visit(SystemVariable variable) throws BioPEPAException {
		switch (variable.getVariable()) {
		case TIME:
			node = new CompiledSystemVariable(CompiledSystemVariable.Variable.TIME);
			break;
		default:
		}
		return true;
	}
}
