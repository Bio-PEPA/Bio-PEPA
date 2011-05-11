/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.LocatedName;
import uk.ac.ed.inf.biopepa.core.dom.Name;
import uk.ac.ed.inf.biopepa.core.dom.VariableDeclaration;

/**
 * @author Mirco
 * 
 */
public class ComponentCompiler extends AbstractDefinitionCompiler {

	ComponentCompiler(ModelCompiler compiler, VariableDeclaration dec) throws CompilerException {
		super(compiler, VariableDeclaration.Kind.COMPONENT, dec);
	}

	@Override
	protected boolean hasDuplicates(String name) {
		return compiler.containsCompartment(name) || compiler.containsCompositionalDefinition(name)
				|| compiler.containsFunctionalRate(name) || compiler.containsVariable(name)
				|| compiler.containsComponent(name);
	}

	@Override
	public ComponentData doGetData() throws BioPEPAException {
		Name name = dec.getName();
		if (name instanceof LocatedName) {
			compiler.problemRequestor.accept(ProblemKind.INVALID_LOCATED_NAME_USE, name);
			throw new CompilerException();
		}
		String identifier = name.getIdentifier();
		ComponentPrefixVisitor v = new ComponentPrefixVisitor(compiler, identifier);
		ComponentData d = new ComponentData(identifier, dec);
		dec.getRightHandSide().accept(v);
		d.setPrefixes(v.getActions());
		return d;
	}
}
