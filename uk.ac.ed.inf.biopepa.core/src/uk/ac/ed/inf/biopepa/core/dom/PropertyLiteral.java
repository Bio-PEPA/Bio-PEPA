/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * @author mtribast, ajduguid
 * 
 */
public class PropertyLiteral extends Expression {

	public enum Kind {

		H(AST.Literals.STEP.getToken()), MAX(AST.Literals.MAX_CONCENTRATION.getToken()), MIN(
				AST.Literals.MIN_CONCENTRATION.getToken()), V(AST.Literals.COMPARTEMENT_PROPERTY.getToken()), SIZE(
				AST.Literals.SIZE.getToken()), TYPE(AST.Literals.TYPE.getToken()), COMPARTMENT(AST.Literals.COMPARTMENT
				.getToken()), MEMBRANE(AST.Literals.MEMBRANE.getToken());

		private String literal;

		Kind(String literal) {
			this.literal = literal;
		}

		public String getLiteral() {
			return literal;
		}

		@Override
		public String toString() {
			return literal;
		}
	}

	private Kind kind;

	/**
	 * @return the kind
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * @param kind
	 *            the kind to set
	 */
	public void setKind(Kind kind) {
		this.kind = kind;
	}

	PropertyLiteral(AST ast) {
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

	/* Property literals obviously cannot contain declaration names */
	public void fillInDeclarationName(Expression _declName) {
		return;
	}
}
