/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import java.util.List;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

public class PrettyPrinterVisitor implements ASTVisitor {

	protected StringBuffer buffer = new StringBuffer();

	public String getString() {
		return buffer.toString();
	}

	public boolean visit(Model model) throws BioPEPAException {
		for (Statement s : model.statements()) {
			PrettyPrinterVisitor v = new PrettyPrinterVisitor();
			s.accept(v);
			buffer.append(v.getString() + "\n");
		}
		return true;
	}

	public boolean visit(VariableDeclaration variableDeclaration) throws BioPEPAException {
		PrettyPrinterVisitor nameVisitor = new PrettyPrinterVisitor();
		variableDeclaration.getName().accept(nameVisitor);
		String token = null;
		String keyword = "";
		if (variableDeclaration.getKind() == VariableDeclaration.Kind.VARIABLE)
			token = " = ";
		else if (variableDeclaration.getKind() == VariableDeclaration.Kind.COMPONENT)
			token = " = ";
		else if (variableDeclaration.getKind() == VariableDeclaration.Kind.SPECIES) {
			token = " : ";
			keyword = "spec ";
		} else if (variableDeclaration.getKind() == VariableDeclaration.Kind.FUNCTION) {
			token = " : ";
			keyword = "func ";
		} else if (variableDeclaration.getKind() == VariableDeclaration.Kind.CONTAINER) {
			token = " : ";
			keyword = "comp ";
		}
		buffer.append(keyword + nameVisitor.getString());
		buffer.append(token);
		PrettyPrinterVisitor variableDefinition = new PrettyPrinterVisitor();
		variableDeclaration.getRightHandSide().accept(variableDefinition);
		buffer.append(variableDefinition.getString());
		return true;
	}

	public boolean visit(Name name) throws BioPEPAException {
		buffer.append(name.getIdentifier());
		return true;
	}

	public boolean visit(PostfixExpression postfixExpression) throws BioPEPAException {
		PrettyPrinterVisitor operand = new PrettyPrinterVisitor();
		postfixExpression.getOperand().accept(operand);

		buffer.append(postfixExpression.getOperator().getLiteral() + " " + operand.getString());
		return true;

	}

	public boolean visit(InfixExpression infixExpression) throws BioPEPAException {
		PrettyPrinterVisitor operand1 = new PrettyPrinterVisitor();
		PrettyPrinterVisitor operand2 = new PrettyPrinterVisitor();
		infixExpression.getLeftHandSide().accept(operand1);
		infixExpression.getRightHandSide().accept(operand2);
		buffer.append(operand1.getString());
		buffer.append(" " + infixExpression.getOperator().getLiteral() + " ");
		buffer.append(operand2.getString());
		return true;
	}

	public boolean visit(NumberLiteral numberLiteral) throws BioPEPAException {
		buffer.append(numberLiteral.getToken());
		return true;

	}

	public boolean visit(Prefix prefix) throws BioPEPAException {
		PrettyPrinterVisitor v1 = new PrettyPrinterVisitor();
		PrettyPrinterVisitor v2 = new PrettyPrinterVisitor();
		prefix.getActionType().accept(v1);
		prefix.getStoichometry().accept(v2);
		buffer.append("(").append(v1.getString());
		buffer.append(", ").append(v2.getString()).append(")");
		return true;
	}

	public boolean visit(Component component) throws BioPEPAException {
		buffer.append(component.getName().getIdentifier());
		buffer.append("[");
		PrettyPrinterVisitor v = new PrettyPrinterVisitor();
		component.getLevel().accept(v);
		buffer.append(v.getString());
		buffer.append("]");
		return true;
	}

	public boolean visit(ExpressionStatement statement) throws BioPEPAException {
		buffer.append("// System equation\n");
		PrettyPrinterVisitor v = new PrettyPrinterVisitor();
		statement.getExpression().accept(v);
		buffer.append(v.getString() + "\n");
		return true;
	}

	public boolean visit(Cooperation cooperation) throws BioPEPAException {
		PrettyPrinterVisitor v1 = new PrettyPrinterVisitor();
		cooperation.getLeftHandSide().accept(v1);
		PrettyPrinterVisitor v2 = new PrettyPrinterVisitor();
		cooperation.getRightHandSide().accept(v2);
		PrettyPrinterVisitor v3 = new PrettyPrinterVisitor();
		cooperation.getActionSet().accept(v3);
		buffer.append(v1.getString()).append(" <");
		buffer.append(v3.getString()).append("> ").append(v2.getString());
		return true;
	}

	public boolean visit(NameSet nameSet) throws BioPEPAException {
		List<Name> locations = nameSet.names();
		int i = locations.size();
		PrettyPrinterVisitor ppv;
		for (Name name : locations) {
			ppv = new PrettyPrinterVisitor();
			name.accept(ppv);
			buffer.append(ppv.getString());
			if (--i > 0)
				buffer.append(", ");
		}
		return true;
	}

	public boolean visit(PropertyLiteral propertyLiteral) throws BioPEPAException {
		buffer.append(propertyLiteral.getKind().getLiteral());
		return true;
	}

	public boolean visit(FunctionCall functionCall) throws BioPEPAException {
		buffer.append(functionCall.getName().getIdentifier());
		buffer.append("(");
		for (int i = 0; i < functionCall.arguments().size(); i++) {
			PrettyPrinterVisitor v = new PrettyPrinterVisitor();
			functionCall.arguments().get(i).accept(v);
			buffer.append(v.getString());
			if (i != functionCall.arguments().size() - 1)
				buffer.append(", ");
		}
		buffer.append(")");
		return true;
	}

	public boolean visit(PropertyInitialiser propertyInitialiser) throws BioPEPAException {
		for (int i = 0; i < propertyInitialiser.properties().size(); i++) {
			PrettyPrinterVisitor v = new PrettyPrinterVisitor();
			propertyInitialiser.properties().get(i).accept(v);
			buffer.append(v.getString());
			if (i != propertyInitialiser.properties().size() - 1)
				buffer.append(", ");
		}
		return true;
	}

	public boolean visit(Transport transport) throws BioPEPAException {
		PrettyPrinterVisitor ppv = new PrettyPrinterVisitor();
		transport.getActonType().accept(ppv);
		buffer.append(ppv.getString());
		buffer.append("[");
		ppv = new PrettyPrinterVisitor();
		transport.getLeftHandSide().accept(ppv);
		buffer.append(ppv.getString()).append(" ");
		buffer.append(transport.getOperator().getLiteral());
		ppv = new PrettyPrinterVisitor();
		transport.getRightHandSide().accept(ppv);
		buffer.append(" ").append(ppv.getString());
		buffer.append("]");
		return true;
	}

	public boolean visit(SystemVariable variable) throws BioPEPAException {
		buffer.append(variable.getVariable().toString());
		return true;
	}
}
