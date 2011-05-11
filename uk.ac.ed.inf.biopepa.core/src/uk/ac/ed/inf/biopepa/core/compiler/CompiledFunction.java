/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import java.util.*;

import uk.ac.ed.inf.biopepa.core.dom.AST;
import uk.ac.ed.inf.biopepa.core.dom.FunctionCall;

public class CompiledFunction extends CompiledExpression {

	/**
	 * Adding a new function
	 * 
	 * 1. Add to dom.AST.Literals. 2. Add to the enum below. 3. Add to the
	 * switch call in getFunctionEvaluator (below) 4. Add to the appropriate
	 * evaluate method in compiler.FunctionEvaluators (static evaluation) 5. Add
	 * to classes that extend CompiledExpressionVisitor (dynamic evaluation)
	 * (following known at time of comment) 1. sba.ISBJava.RatesVisitor 2.
	 * sba.SBAModel.SystemEquationVisitor.FunctionChecker (needs changing if a
	 * function isn't differentiable) 3. sba.export.SBMLExport.SBMLRateGenerator
	 * 4. sba.export.SBMLExport.SBMLParseable 5.
	 * sba.export.SBMLExport.ParameterVisitor (shouldn't need changing) 6.
	 * analysis.ReactantRateParticipationCheck.ReactantParticipantVisitor
	 * (shouldn't need changing)
	 * 
	 * @author ajduguid
	 * 
	 */

	public enum Function {
		LOG(AST.Literals.LOGARITHM.getToken(), 1, false), EXP(AST.Literals.EXP.getToken(), 1, false), H(
				AST.Literals.HEAVISIDE.getToken(), 1, false), FLOOR(AST.Literals.FLOOR.getToken(), 1, false), CEILING(
				AST.Literals.CEILING.getToken(), 1, false), fMA(AST.Literals.MASS_ACTION.getToken(), 1, true), fMM(
				AST.Literals.MICHAELIS_MENTEN.getToken(), 2, true), TANH(AST.Literals.TANH.getToken(), 1, false);

		int arg;
		String name;
		boolean rate;

		Function(String name, int arg, boolean rate) {
			this.arg = arg;
			this.name = name;
			this.rate = rate;
		}

		public String getID() {
			return name;
		}

		public boolean isRateLaw() {
			return rate;
		}

		public int args() {
			return arg;
		}
	}

	public static Function getFunction(String function) {
		for (Function f : Function.values())
			if (f.name.equals(function))
				return f;
		return null;
	}

	Function function = null;

	List<CompiledExpression> arguments = new ArrayList<CompiledExpression>(3);

	public Function getFunction() {
		return function;
	}

	void setFunction(Function function) {
		if (arguments.size() > function.arg)
			throw new IllegalStateException();
		this.function = function;
	}

	void setArgument(int index, CompiledExpression argument) {
		if (function != null && function.arg < index)
			throw new IllegalArgumentException();
		for (int i = arguments.size(); i <= index; i++)
			arguments.add(null);
		arguments.set(index, argument);
	}

	public List<CompiledExpression> getArguments() {
		return Collections.unmodifiableList(arguments);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(function.name).append("(");
		for (CompiledExpression en : arguments)
			sb.append(en.toString()).append(", ");
		sb.replace(sb.length() - 2, sb.length(), ")");
		return sb.toString();
	}

	public static Function checkFunction(ModelCompiler compiler, FunctionCall call) throws CompilerException {
		Function f = getFunction(call.getName().getIdentifier());
		if (f == null) {
			compiler.problemRequestor.accept(ProblemKind.UNSUPPORTED_FUNCTION_USED, call);
			throw new CompilerException();
		}
		if (call.arguments().size() != f.arg) {
			compiler.problemRequestor.accept(ProblemKind.INVALID_NUMBER_OF_ARGUMENTS, call);
			throw new CompilerException();
		}
		return f;
	}

	public static IPredefinedFunctionEvaluator 
			getFunctionEvaluator(ModelCompiler compiler, FunctionCall call)
				throws CompilerException {
		Function f = checkFunction(compiler, call);
		switch (f) {
		case LOG:
		case EXP:
		case H:
		case FLOOR:
		case CEILING:
		case TANH:
			return new FunctionEvaluators.GenericOneArgumentFunction(compiler, call);
		default:
			throw new IllegalStateException();
		}
	}

	public boolean accept(CompiledExpressionVisitor visitor) {
		return visitor.visit(this);
	}

	public boolean isDynamic () {
		// Technically we probably should be a bit more
		// savvy than this, there might be some function
		// that doesn't depend on dynamic arguments in which case
		// we would need to look at the function and possibly the
		// arguments, for example fMA can always be considered
		// dynamic regardless of what the arguments are (they are likely
		// to be static). But if we had a function such as cos then
		// we would only need to look at the arguments
		return true;
		/*
		for (CompiledExpression ce : arguments){
			if (ce != null && ce.isDynamic()){
				return true;
			}
		}
		return false;
		*/
	}
	
	public CompiledFunction clone() {
		CompiledFunction cf = new CompiledFunction();
		cf.function = function;
		for (CompiledExpression ce : arguments) {
			if (ce != null)
				cf.arguments.add(ce.clone());
			else
				cf.arguments.add(null);
		}
		if (expandedForm != null)
			cf.expandedForm = expandedForm.clone();
		return cf;
	}
}
