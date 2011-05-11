/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.*;

/**
 * Visits the definition of a species.
 * 
 * @author Mirco
 * 
 */
public class SpeciesDefinitionCompiler extends AbstractDefinitionCompiler {

	public SpeciesDefinitionCompiler(ModelCompiler compiler, VariableDeclaration dec) {
		super(compiler, VariableDeclaration.Kind.SPECIES, dec);
	}

	private List<SpeciesData> getSpeciesData() throws BioPEPAException {
		Name name = dec.getName();
		List<SpeciesData> species = new ArrayList<SpeciesData>();
		SpeciesData data;
		if (name instanceof LocatedName) {
			LocatedName lName = ((LocatedName) name);
			List<Name> names = lName.getLocations().names();
			CompartmentData compartmentData;
			for (int i = names.size() - 1; i >= 0; i--) {
				compartmentData = compiler.checkAndGetCompartmentData(names.get(i).getIdentifier());
				if (compartmentData == null) {
					compiler.problemRequestor.accept(ProblemKind.LOCATION_USED_BUT_NOT_DEFINED, name);
					throw new CompilerException();
				}
				data = new SpeciesData(lName.getIdentifier(i), dec);
				data.setCompartment(compartmentData);
				species.add(data);
			}
		} else {
			data = new SpeciesData(name.getIdentifier(), dec);
			species.add(data);
		}
		PropertyInitialiser init = (PropertyInitialiser) dec.getRightHandSide();
		CompiledNumber enn;
		for (Expression expression : init.properties()) {
			if (!(expression instanceof InfixExpression))
				throw new IllegalArgumentException("Expected an infix expression");
			InfixExpression property = (InfixExpression) expression;
			if (!(property.getLeftHandSide() instanceof PropertyLiteral))
				throw new IllegalArgumentException("Expected a property literal");
			PropertyLiteral literal = (PropertyLiteral) property.getLeftHandSide();
			try {
				if (species.get(0).isSetProperty(literal)) {
					compiler.problemRequestor.accept(ProblemKind.DUPLICATE_PROPERTY_DEFINITION, literal);
					continue;
				}
			} catch (IllegalArgumentException e) {
				compiler.problemRequestor.accept(ProblemKind.ILLEGAL_PROPERTY, literal);
				continue;
			}
			ExpressionEvaluatorVisitor v = new ExpressionEvaluatorVisitor(compiler);
			property.getRightHandSide().accept(v);
			if (!(v.getExpressionNode() instanceof CompiledNumber)) {
				compiler.problemRequestor.accept(ProblemKind.DYNAMIC_VALUE, property);
				throw new CompilerException();
			}
			enn = (CompiledNumber) v.getExpressionNode();
			if (!enn.evaluatesToLong()) {
				compiler.problemRequestor.accept(ProblemKind.NON_INTEGER_VALUE, property);
				throw new CompilerException();
			}
			try {
				for (SpeciesData sd : species)
					sd.setProperty(literal, enn.longValue());
			} catch (PropertySetterException e) {
				compiler.problemRequestor.accept(e.getExplanation(), property);
				throw new CompilerException();
			}
		}
		if (!species.get(0).isSetProperty(PropertyLiteral.Kind.MAX)) {
			compiler.problemRequestor.accept(ProblemKind.MAXIMUM_COUNT_MUST_BE_SPECIFIED, dec);
			throw new CompilerException();
		}
		return species;
	}

	@Override
	/**
	 * Throws UnsupportedOperationException()
	 */
	protected Data doGetData() throws BioPEPAException {
		throw new UnsupportedOperationException();
	}

	protected List<SpeciesData> doGetDataList() throws BioPEPAException {
		return getSpeciesData();
	}

}
