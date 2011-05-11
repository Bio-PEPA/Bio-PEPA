/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

public class SystemVariable extends Expression {

	public enum Variable {
		TIME(AST.Literals.TIME.toString());

		String name;

		Variable(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

	Variable variable;

	SystemVariable(AST ast) {
		super(ast);
	}

	@Override
	public void accept(ASTVisitor visitor) throws BioPEPAException {
		visitor.visit(this);
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}

	public Variable getVariable() {
		return variable;
	}

	/* System variables cannot contain declaration names */
	public void fillInDeclarationName(Expression _declName) {
		return;
	}
}
