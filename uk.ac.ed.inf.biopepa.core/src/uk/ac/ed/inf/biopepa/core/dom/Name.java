/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * A name in the syntax tree. It may represent function names, constant, as well
 * as component names.
 * 
 * @author mtribast
 * 
 */
public class Name extends Expression {

	private String identifier;

	Name(AST ast) {
		super(ast);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTNode#accept(uk.ac.ed.inf.biopepa.core
	 * .dom.ASTVisitor)
	 */
	@Override
	public void accept(ASTVisitor visitor) throws BioPEPAException {
		visitor.visit(this);
	}

	/**
	 * Returns the identifier, i.e. the token defining the name.
	 * 
	 * @return the identifier, i.e. the token defining the name
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Sets the identifier, i.e. the token defining the name
	 * 
	 * @param identifier
	 *            the token defining the name
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Gets the binding for this name
	 * 
	 * @return the binding for this name, or null
	 */
	public IBinding getBinding() {
		return ast.getBindingResolver().resolveName(identifier);
	}

	/* If the name is present at all it cannot contain a declaration name */
	public void fillInDeclarationName(Expression _declName) {
		return;
	}
}
