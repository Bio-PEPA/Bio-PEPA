/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

/**
 * Super class of expressions
 * 
 * @author mtribast
 * 
 */
public abstract class Expression extends ASTNode {

	Expression(AST ast) {
		super(ast);
	}

	/*
	 * The grammar allows for a short cut in which if the name of the behaviour
	 * being defined is the name of the right hand side of a prefix then it may
	 * be omitted. This function allows us to fill in those omitted names with
	 * the name of the declaration.
	 */
	public abstract void fillInDeclarationName(Expression declName);
}
