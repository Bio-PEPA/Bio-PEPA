/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * 
 * @author ajduguid
 * 
 */
public abstract class DoNothingVisitor implements ASTVisitor {

	public boolean visit(NameSet actionSet) throws BioPEPAException {
		return false;
	}

	public boolean visit(Component component) throws BioPEPAException {
		return false;
	}

	public boolean visit(Cooperation cooperation) throws BioPEPAException {
		return false;
	}

	public boolean visit(ExpressionStatement statement) throws BioPEPAException {
		return false;
	}

	public boolean visit(FunctionCall functionCall) throws BioPEPAException {
		return false;
	}

	public boolean visit(InfixExpression infixExpression) throws BioPEPAException {
		return false;
	}

	public boolean visit(Model model) throws BioPEPAException {
		return false;
	}

	public boolean visit(Name name) throws BioPEPAException {
		return false;
	}

	public boolean visit(NumberLiteral numberLiteral) throws BioPEPAException {
		return false;
	}

	public boolean visit(PostfixExpression postfixExpression) throws BioPEPAException {
		return false;
	}

	public boolean visit(Prefix prefix) throws BioPEPAException {
		return false;
	}

	public boolean visit(PropertyInitialiser propertyInitialiser) throws BioPEPAException {
		return false;
	}

	public boolean visit(PropertyLiteral propertyLiteral) throws BioPEPAException {
		return false;
	}

	public boolean visit(VariableDeclaration variableDeclaration) throws BioPEPAException {
		return false;
	}

	public boolean visit(Transport transport) throws BioPEPAException {
		return false;
	}

	public boolean visit(SystemVariable variable) throws BioPEPAException {
		return false;
	}
}
