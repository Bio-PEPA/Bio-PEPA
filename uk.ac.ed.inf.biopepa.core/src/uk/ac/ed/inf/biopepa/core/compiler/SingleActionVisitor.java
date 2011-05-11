/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import java.util.List;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.*;
import uk.ac.ed.inf.biopepa.core.compiler.PrefixData.Operator;

/**
 * @author Mirco
 * @author ajduguid
 * 
 */
public class SingleActionVisitor extends DefaultCompilerVisitor {

	PrefixData data = null;

	public SingleActionVisitor(ModelCompiler compiler) {
		super(compiler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.Name)
	 */
	@Override
	public boolean visit(Name name) throws BioPEPAException {
		String id = name.getIdentifier();
		if (data == null) // shorthand notation used
			data = new ActionData();
		data.setFunction(id);
		if (null == compiler.checkAndGetFunctionalRate(id)) {
			compiler.problemRequestor.accept(ProblemKind.FUNCTIONAL_RATE_USED_BUT_NOT_DECLARED, name);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.dom.ASTVisitor#visit(uk.ac.ed.inf.biopepa.core
	 * .dom.Prefix)
	 */
	@Override
	public boolean visit(Prefix prefix) throws BioPEPAException {
		data = new ActionData();
		prefix.getActionType().accept(this);
		Expression stoichometry = prefix.getStoichometry();
		ExpressionEvaluatorVisitor v = new ExpressionEvaluatorVisitor(compiler);
		stoichometry.accept(v);
		if (!(v.getExpressionNode() instanceof CompiledNumber)) {
			compiler.problemRequestor.accept(ProblemKind.DYNAMIC_VALUE, stoichometry);
			return false;
		}
		CompiledNumber enn = (CompiledNumber) v.getExpressionNode();
		if (!enn.evaluatesToLong()) {
			compiler.problemRequestor.accept(ProblemKind.NON_INTEGER_VALUE, stoichometry);
			return false;
		}
		data.setStoichometry(enn.longValue());
		return true;
	}

	public boolean visit(Transport transport) throws BioPEPAException {
		TransportData td = new TransportData();
		data = td;
		transport.getActonType().accept(this);
		Name name = (Name) transport.getLeftHandSide();
		String id = name.getIdentifier();
		if (null == compiler.checkAndGetCompartmentData(id)) {
			compiler.problemRequestor.accept(ProblemKind.LOCATION_USED_BUT_NOT_DEFINED, name);
			return false;
		}
		td.setSourceLocation(id);
		name = (Name) transport.getRightHandSide();
		id = name.getIdentifier();
		if (null == compiler.checkAndGetCompartmentData(id)) {
			compiler.problemRequestor.accept(ProblemKind.LOCATION_USED_BUT_NOT_DEFINED, name);
			return false;
		}
		td.setTargetLocation(id);
		switch (transport.getOperator()) {
		case BMOVE:
			td.setOperator(Operator.BI_TRANSPORTATION);
			break;
		case UMOVE:
			td.setOperator(Operator.UNI_TRANSPORTATION);
			break;
		default:
			compiler.problemRequestor.accept(ProblemKind.INVALID_OPERATOR_FOR_TRANSPORT, transport);
			return false;
		}
		return true;
	}

	public boolean visit(InfixExpression infixExpression) throws BioPEPAException {
		infixExpression.getLeftHandSide().accept(this);
		data.declaration = infixExpression;
		Name name = (Name) infixExpression.getRightHandSide();
		if (data instanceof TransportData) { // transportation
			if (infixExpression.getOperator() != InfixExpression.Operator.GENERIC) {
				compiler.problemRequestor.accept(ProblemKind.INVALID_OPERATOR_FOR_TRANSPORT,
						". Generic operator should be used to denote transportation", infixExpression);
				return false;
			}
			if (name instanceof LocatedName) {
				compiler.problemRequestor.accept(ProblemKind.INVALID_LOCATED_NAME_USE, name);
				return false;
			}
		} else { // must be a ActionData
			if (name instanceof LocatedName) {
				List<Name> names = ((LocatedName) name).getLocations().names();
				ActionData pd = (ActionData) data;
				String s;
				for (Name n : names) {
					s = n.getIdentifier();
					if (null == compiler.checkAndGetCompartmentData(s)) {
						compiler.problemRequestor.accept(ProblemKind.LOCATION_USED_BUT_NOT_DEFINED, n);
						return false;
					}
					pd.addLocation(s);
				}
			}
			switch (infixExpression.getOperator()) {
			case ACTIVATOR:
				data.setOperator(Operator.ACTIVATOR);
				break;
			case GENERIC:
				data.setOperator(Operator.GENERIC);
				break;
			case INHIBITOR:
				data.setOperator(Operator.INHIBITOR);
				break;
			case PRODUCT:
				data.setOperator(Operator.PRODUCT);
				break;
			case REACTANT:
				data.setOperator(Operator.REACTANT);
				break;
			default:
				compiler.problemRequestor.accept(ProblemKind.INVALID_OPERATOR_FOR_REACTION, infixExpression);
				return false;
			}
		}
		return true;
	}
}
