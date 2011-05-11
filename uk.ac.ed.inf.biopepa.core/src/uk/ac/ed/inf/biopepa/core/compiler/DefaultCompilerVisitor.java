/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.*;

/**
 * A default compiler visitor which throws {@link IllegalArgumentException}s and
 * has a reference to the compiler.
 * 
 * @author Mirco
 * 
 */
public abstract class DefaultCompilerVisitor implements ASTVisitor {

	protected ModelCompiler compiler;

	protected DefaultCompilerVisitor(ModelCompiler compiler) {
		if (compiler == null)
			throw new NullPointerException();
		this.compiler = compiler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.Model)
	 */
	public boolean visit(Model model) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.VariableDeclaration)
	 */
	public boolean visit(VariableDeclaration variableDeclaration) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.Name)
	 */
	public boolean visit(Name name) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.PostfixExpression)
	 */
	public boolean visit(PostfixExpression postfixExpression) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.InfixExpression)
	 */
	public boolean visit(InfixExpression infixExpression) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.NumberLiteral)
	 */
	public boolean visit(NumberLiteral numberLiteral) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.Prefix)
	 */
	public boolean visit(Prefix prefix) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.Component)
	 */
	public boolean visit(Component component) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.ExpressionStatement)
	 */
	public boolean visit(ExpressionStatement statement) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.Cooperation)
	 */
	public boolean visit(Cooperation cooperation) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.ActionSet)
	 */
	public boolean visit(NameSet actionSet) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.PropertyLiteral)
	 */
	public boolean visit(PropertyLiteral propertyLiteral) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.FunctionCall)
	 */
	public boolean visit(FunctionCall functionCall) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.PropertyInitialiser)
	 */
	public boolean visit(PropertyInitialiser propertyInitialiser) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	public boolean visit(Transport transport) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

	public boolean visit(SystemVariable variable) throws BioPEPAException {
		throw new IllegalArgumentException();
	}

}
