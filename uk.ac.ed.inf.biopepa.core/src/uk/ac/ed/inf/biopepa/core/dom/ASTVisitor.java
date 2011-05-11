/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * Visitor interface for the AST of BioPEPA
 * 
 * @author mtribast
 * 
 */
public interface ASTVisitor {

	boolean visit(NameSet nameSet) throws BioPEPAException;

	boolean visit(Component component) throws BioPEPAException;

	boolean visit(Cooperation cooperation) throws BioPEPAException;

	boolean visit(ExpressionStatement statement) throws BioPEPAException;

	boolean visit(FunctionCall functionCall) throws BioPEPAException;

	boolean visit(InfixExpression infixExpression) throws BioPEPAException;

	boolean visit(Model model) throws BioPEPAException;

	boolean visit(Name name) throws BioPEPAException;

	boolean visit(NumberLiteral numberLiteral) throws BioPEPAException;

	boolean visit(PostfixExpression postfixExpression) throws BioPEPAException;

	boolean visit(Prefix prefix) throws BioPEPAException;

	boolean visit(PropertyInitialiser propertyInitialiser) throws BioPEPAException;

	boolean visit(PropertyLiteral propertyLiteral) throws BioPEPAException;

	boolean visit(VariableDeclaration variableDeclaration) throws BioPEPAException;

	boolean visit(Transport transport) throws BioPEPAException;

	boolean visit(SystemVariable variable) throws BioPEPAException;
}
