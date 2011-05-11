/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.*;

/**
 * Visits a compartment definition
 * 
 * @author mtribast
 * 
 */
public class CompartmentCompiler extends AbstractDefinitionCompiler {

	public CompartmentCompiler(ModelCompiler compiler, VariableDeclaration dec) {
		super(compiler, VariableDeclaration.Kind.CONTAINER, dec);
	}

	private CompartmentData getCompartmentData() throws BioPEPAException {
		PropertyInitialiser init = (PropertyInitialiser) dec.getRightHandSide();
		Name name = dec.getName();
		CompartmentData data;
		if (name instanceof LocatedName) {
			LocatedName ln = (LocatedName) name;
			if (ln.getLocations().names().size() > 1) {
				compiler.problemRequestor.accept(ProblemKind.INVALID_NUMBER_OF_LOCATIONS, name);
				throw new CompilerException();
			}
			data = new CompartmentData(ln.getName(), dec);
			data.setParent(ln.getLocations().names().get(0).getIdentifier());
		} else
			data = new CompartmentData(dec.getName().getIdentifier(), dec);
		for (Expression expression : init.properties()) {
			if (!(expression instanceof InfixExpression))
				throw new IllegalArgumentException("Expected an infix expression");
			InfixExpression property = (InfixExpression) expression;
			if (!(property.getLeftHandSide() instanceof PropertyLiteral))
				throw new IllegalArgumentException("Expected a property literal");
			PropertyLiteral literal = (PropertyLiteral) property.getLeftHandSide();
			try {
				if (literal.getKind() == PropertyLiteral.Kind.TYPE) {
					if (!(property.getRightHandSide() instanceof PropertyLiteral))
						throw new IllegalArgumentException("Expected a property literal");
					data.setProperty(literal, (PropertyLiteral) property.getRightHandSide());
				} else {
					ExpressionEvaluatorVisitor v = new ExpressionEvaluatorVisitor(compiler);
					property.getRightHandSide().accept(v);
					if (!(v.getExpressionNode() instanceof CompiledNumber)) {
						compiler.problemRequestor.accept(ProblemKind.DYNAMIC_VALUE, property);
						throw new CompilerException();
					}
					data.setProperty(literal, ((CompiledNumber) v.getExpressionNode()).doubleValue());
				}
			} catch (PropertySetterException e) {
				compiler.problemRequestor.accept(e.getExplanation(), property);
				throw new CompilerException();
			}
		}
		return data;
	}

	@Override
	protected Data doGetData() throws BioPEPAException {
		return getCompartmentData();
	}

}
