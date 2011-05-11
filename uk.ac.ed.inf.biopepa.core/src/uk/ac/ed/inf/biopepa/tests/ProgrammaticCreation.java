/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.tests;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.AST;
import uk.ac.ed.inf.biopepa.core.dom.NameSet;
import uk.ac.ed.inf.biopepa.core.dom.Component;
import uk.ac.ed.inf.biopepa.core.dom.Cooperation;
import uk.ac.ed.inf.biopepa.core.dom.ExpressionStatement;
import uk.ac.ed.inf.biopepa.core.dom.FunctionCall;
import uk.ac.ed.inf.biopepa.core.dom.IBinding;
import uk.ac.ed.inf.biopepa.core.dom.InfixExpression;
import uk.ac.ed.inf.biopepa.core.dom.Model;
import uk.ac.ed.inf.biopepa.core.dom.Name;
import uk.ac.ed.inf.biopepa.core.dom.NumberLiteral;
import uk.ac.ed.inf.biopepa.core.dom.Prefix;
import uk.ac.ed.inf.biopepa.core.dom.PrettyPrinterVisitor;
import uk.ac.ed.inf.biopepa.core.dom.PropertyInitialiser;
import uk.ac.ed.inf.biopepa.core.dom.PropertyLiteral;
import uk.ac.ed.inf.biopepa.core.dom.VariableDeclaration;

public class ProgrammaticCreation {

	/**
	 * @param args
	 * @throws BioPEPAException
	 */
	public static void main(String[] args) throws BioPEPAException {
		String x = "X", y = "Y", z = "Z";
		String alpha = "alpha", r = "r";
		String v = "V";
		AST ast = AST.newAST();
		Model model = ast.newModel();
		VariableDeclaration d2 = createConstant(ast, v, 1);
		VariableDeclaration d1 = createConstant(ast, r, 0.001);

		model.statements().add(d2);
		model.statements().add(createProperties(ast, x, 200, v));
		model.statements().add(createProperties(ast, y, 100, v));
		model.statements().add(createProperties(ast, z, 300, v));

		model.statements().add(d1);

		FunctionCall fc = ast.newFunctionCall();
		fc.setName(createName(ast, "fMA"));
		Name parameter = createName(ast, r);
		fc.arguments().add(parameter);

		VariableDeclaration function = ast.newVariableDeclaration();
		function.setKind(VariableDeclaration.Kind.FUNCTION);
		function.setName(createName(ast, alpha));
		function.setRightHandSide(fc);

		model.statements().add(function);

		model.statements().add(createSimpleSequential(ast, x, alpha, InfixExpression.Operator.REACTANT, 2));
		model.statements().add(createSimpleSequential(ast, y, alpha, InfixExpression.Operator.REACTANT, 1));
		model.statements().add(createSimpleSequential(ast, z, alpha, InfixExpression.Operator.PRODUCT, 3));

		Cooperation firstCoop = ast.newCooperation();
		firstCoop.setActionSet(createActionSet(ast, alpha));
		firstCoop.setLeftHandSide(createComponent(ast, x, 2));
		firstCoop.setRightHandSide(createComponent(ast, y, 1));

		Cooperation secondCoop = ast.newCooperation();
		secondCoop.setActionSet(createActionSet(ast, alpha));
		secondCoop.setLeftHandSide(firstCoop);
		secondCoop.setRightHandSide(createComponent(ast, z, 0));

		ExpressionStatement statement = ast.newExpressionStatement();
		statement.setExpression(secondCoop);
		model.statements().add(statement);

		PrettyPrinterVisitor visitor = new PrettyPrinterVisitor();
		model.accept(visitor);
		System.out.println(visitor.getString());

		checkName(ast, "X");
		checkName(ast, "Y");
		checkName(ast, "s");
	}

	private static void checkName(AST ast, String name) {
		Name n = createName(ast, name);
		IBinding b = n.getBinding();
		System.out.println(name + ": " + ((b != null) ? b.getVariableDeclaration().getKind() : b));
	}

	/**
	 * Creates:
	 * 
	 * <pre>
	 * &lt; action &gt;
	 * </pre>
	 * 
	 * @param ast
	 * @param action
	 * @return
	 */
	private static NameSet createActionSet(AST ast, String action) {
		NameSet set = ast.newNameSet();
		set.names().add(createName(ast, action));
		return set;
	}

	/**
	 * Creates: name(level)
	 * 
	 * @param ast
	 * @param name
	 * @param level
	 * @return
	 */
	private static Component createComponent(AST ast, String name, int level) {
		Component component = ast.newComponent();
		component.setName(createName(ast, name));
		NumberLiteral l = ast.newNumberLiteral();
		l.setToken(Integer.toString(level));
		component.setLevel(l);
		return component;

	}

	private static VariableDeclaration createConstant(AST ast, String name, double value) {
		VariableDeclaration declaration = ast.newVariableDeclaration();
		declaration.setKind(VariableDeclaration.Kind.VARIABLE);
		declaration.setName(createName(ast, name));

		NumberLiteral literal = ast.newNumberLiteral();
		literal.setToken(Double.toString(value));
		declaration.setRightHandSide(literal);
		return declaration;
	}

	/*
	 * Creates: <X> = (<action>, <stoichometry>) <operand> <X>
	 */
	private static VariableDeclaration createSimpleSequential(AST ast, String name, String action,
			InfixExpression.Operator operator, int stoichometry) {
		VariableDeclaration declaration = ast.newVariableDeclaration();
		declaration.setKind(VariableDeclaration.Kind.COMPONENT);
		declaration.setName(createName(ast, name));

		InfixExpression expression = ast.newInfixExpression();
		expression.setOperator(operator);

		Prefix prefix = ast.newPrefix();
		prefix.setActionType(createName(ast, action));
		NumberLiteral literal = ast.newNumberLiteral();
		literal.setToken(Integer.toString(stoichometry));
		prefix.setStoichometry(literal);
		expression.setLeftHandSide(prefix);

		expression.setRightHandSide(createName(ast, name));

		declaration.setRightHandSide(expression);
		return declaration;
	}

	/**
	 * Creates:
	 * 
	 * <pre>
	 * &lt;name&gt; : { H = &lt;h&gt;, N = &lt;n&gt;, M_0 = &lt;m_0&gt;, etc }
	 * </pre>
	 * 
	 * @param ast
	 * @param name
	 * @param h
	 * @param n
	 * @param m_0
	 * @param m
	 * @param v
	 * @return
	 */
	private static VariableDeclaration createProperties(AST ast, String name, int max, String v) {
		VariableDeclaration declaration = ast.newVariableDeclaration();
		declaration.setKind(VariableDeclaration.Kind.SPECIES);
		declaration.setName(createName(ast, name));

		PropertyInitialiser initialiser = ast.newPropertyInitialiser();
		initialiser.properties().add(createProperty(ast, PropertyLiteral.Kind.MAX, max));
		InfixExpression expression = ast.newInfixExpression();
		PropertyLiteral literal = ast.newPropertyLiteral();
		literal.setKind(PropertyLiteral.Kind.V);
		expression.setLeftHandSide(literal);
		expression.setOperator(InfixExpression.Operator.EQUALS);
		expression.setRightHandSide(createName(ast, v));
		initialiser.properties().add(expression);
		declaration.setRightHandSide(initialiser);
		return declaration;
	}

	private static InfixExpression createProperty(AST ast, PropertyLiteral.Kind kind, double value) {
		InfixExpression expression = ast.newInfixExpression();
		PropertyLiteral literal = ast.newPropertyLiteral();
		literal.setKind(kind);
		expression.setLeftHandSide(literal);

		expression.setOperator(InfixExpression.Operator.EQUALS);
		NumberLiteral number = ast.newNumberLiteral();
		number.setToken(Double.toString(value));
		expression.setRightHandSide(number);
		return expression;
	}

	private static Name createName(AST ast, String name) {
		Name variableName = ast.newName();
		variableName.setIdentifier(name);
		return variableName;

	}

}
