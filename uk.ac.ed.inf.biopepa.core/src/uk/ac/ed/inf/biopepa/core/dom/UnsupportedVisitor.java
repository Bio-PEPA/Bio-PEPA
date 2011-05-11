/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * Template class. Any method not over-ridden will throw an
 * UnsupportOperationException.
 * 
 * @author ajduguid
 * 
 */
public class UnsupportedVisitor implements ASTVisitor {

	public boolean visit(NameSet nameSet) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(Component component) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(Cooperation cooperation) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(ExpressionStatement statement) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(FunctionCall functionCall) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(InfixExpression infixExpression) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(Model model) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(Name name) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(NumberLiteral numberLiteral) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(PostfixExpression postfixExpression) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(Prefix prefix) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(PropertyInitialiser propertyInitialiser) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(PropertyLiteral propertyLiteral) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(VariableDeclaration variableDeclaration) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(Transport transport) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	public boolean visit(SystemVariable variable) throws BioPEPAException {
		throw new UnsupportedOperationException();
	}
}
