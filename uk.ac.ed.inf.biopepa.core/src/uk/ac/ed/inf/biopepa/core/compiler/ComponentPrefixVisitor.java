/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import java.util.ArrayList;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.*;

/**
 * 
 * @author ajduguid
 * 
 */
public class ComponentPrefixVisitor extends DefaultCompilerVisitor {

	private String identifier;

	private ArrayList<PrefixData> prefixes;

	public ComponentPrefixVisitor(ModelCompiler compiler, String identifier) {
		this(compiler, identifier, new ArrayList<PrefixData>());
	}

	private ComponentPrefixVisitor(ModelCompiler compiler, String identifier, ArrayList<PrefixData> prefixes) {
		super(compiler);
		this.identifier = identifier;
		this.prefixes = prefixes;
	}

	public PrefixData[] getActions() {
		return prefixes.toArray(new PrefixData[prefixes.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.InfixExpression)
	 */
	@Override
	public boolean visit(InfixExpression infixExpression) throws BioPEPAException {
		// handling choice
		if (infixExpression.getOperator() == InfixExpression.Operator.PLUS) {
			return handleChoice(infixExpression);
		}
		Expression rhs = infixExpression.getRightHandSide();
		if (!(rhs instanceof Name)) {
			compiler.problemRequestor.accept(ProblemKind.INVALID_TARGET_COMPONENT, rhs);
			// it is a problem
			throw new CompilerException();
		}
		if (rhs instanceof LocatedName) {
			LocatedName ln = (LocatedName) rhs;
			if (!ln.getName().equals(identifier)) {
				compiler.problemRequestor.accept(ProblemKind.INVALID_TARGET_COMPONENT, rhs);
				throw new CompilerException();
			}
			for (int i = ln.getLocations().names().size() - 1; i >= 0; i--)
				if (!compiler.isDynamic(ln.getIdentifier(i))) {
					if (ln.getLocations().names().size() == 1)
						compiler.problemRequestor.accept(ProblemInfo.Severity.WARNING, "Action is undefined as "
								+ ln.getIdentifier(i) + " is not declared in the model component.", infixExpression);
					else
						compiler.problemRequestor.accept(ProblemInfo.Severity.WARNING, "Action is undefined as "
								+ ln.getIdentifier(i) + " is not declared in the model component.", ln);
				}
		} else if (!((Name) rhs).getIdentifier().equals(identifier)) {
			compiler.problemRequestor.accept(ProblemKind.INVALID_TARGET_COMPONENT, rhs);
			throw new CompilerException();
		}
		// rhs is an OK name
		// now lhs must be a prefix
		SingleActionVisitor v = new SingleActionVisitor(compiler);
		infixExpression.accept(v);
		checkAndAdd(infixExpression.getLeftHandSide(), v.data);
		return true;
	}

	private void checkAndAdd(Expression lhs, PrefixData data) throws BioPEPAException {
		for (PrefixData insertedData : prefixes) {
			if (insertedData.function.equals(data.function)) {
				compiler.problemRequestor.accept(ProblemKind.DUPLICATE_REACTION_FOUND, lhs);
				throw new CompilerException();
			}
		}
		this.prefixes.add(data);
	}

	private boolean handleChoice(InfixExpression infixExpression) throws BioPEPAException {
		ComponentPrefixVisitor lhs = new ComponentPrefixVisitor(compiler, identifier, prefixes);
		infixExpression.getLeftHandSide().accept(lhs);
		ComponentPrefixVisitor rhs = new ComponentPrefixVisitor(compiler, identifier, prefixes);
		infixExpression.getRightHandSide().accept(rhs);
		return true;
	}
}
