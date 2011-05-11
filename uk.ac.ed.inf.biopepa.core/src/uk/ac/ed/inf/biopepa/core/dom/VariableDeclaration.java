/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * A declaration of a variable.
 * 
 * @author mtribast
 * 
 */
public class VariableDeclaration extends Statement {

	/**
	 * The kind of variable declaration represented by this node
	 * 
	 * @author mtribast
	 * 
	 */
	public enum Kind {
		/**
		 * Variable declaration, such as constant k = 1.0;
		 */
		VARIABLE,

		/**
		 * Component declaration, i.e.
		 * 
		 * <pre>
		 * 	M = (alpha, 1) (+) M
		 * </pre>
		 */
		COMPONENT,

		/**
		 * Function declaration, i.e.
		 * 
		 * <pre>
		 * func alpha = [K * M / 12];
		 * </pre>
		 */
		FUNCTION,

		/**
		 * Property declaration, i.e.
		 * 
		 * <pre>
		 * species ASpecies : H = 1, M_0 = 2;
		 * </pre>
		 */
		SPECIES,

		/**
		 * Property declaration, i.e.
		 * 
		 * <pre>
		 * comp Comp : size = 1;
		 * </pre>
		 */
		CONTAINER

	}

	private Kind kind;

	private Name name;

	private Expression rightHandSide;

	VariableDeclaration(AST ast) {
		super(ast);
	}

	/**
	 * Gets the kind of declaration for this node
	 * 
	 * @return the kind of declaration for this node.
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * Gets the right hand side, i.e. the variable definition
	 * 
	 * @return the right hand side, i.e. the variable definition
	 */
	public Expression getRightHandSide() {
		return rightHandSide;
	}

	/**
	 * Sets the right hand side, i.e. the variable definition
	 */
	public void setRightHandSide(Expression rightHandSide) {
		this.rightHandSide = rightHandSide;
	}

	/**
	 * Sets the kind of declaration for this node
	 * 
	 * @param kind
	 *            the kind of declaration for this node.
	 */
	public void setKind(Kind kind) {
		this.kind = kind;
	}

	@Override
	public void accept(ASTVisitor visitor) throws BioPEPAException {
		visitor.visit(this);
	}

	/**
	 * @return the name
	 */
	public Name getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(Name name) {
		this.name = name;
	}
}
