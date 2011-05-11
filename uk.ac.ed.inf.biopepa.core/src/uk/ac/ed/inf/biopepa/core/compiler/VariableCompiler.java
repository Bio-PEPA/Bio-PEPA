/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.VariableDeclaration;

public class VariableCompiler extends AbstractDefinitionCompiler {

	public VariableCompiler(ModelCompiler compiler, VariableDeclaration dec) {
		super(compiler, VariableDeclaration.Kind.VARIABLE, dec);
	}

	@Override
	protected Data doGetData() throws BioPEPAException {
		String name = dec.getName().getIdentifier();
		if (name == null)
			throw new IllegalArgumentException("Variable declaration does not contain left hand side");
		VariableData result = new VariableData(name, dec);
		ExpressionEvaluatorVisitor v = new ExpressionEvaluatorVisitor(compiler);
		dec.getRightHandSide().accept(v);
		result.setValue(v.getExpressionNode());
		return result;
	}

}
