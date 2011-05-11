/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.PropertyInitialiser;
import uk.ac.ed.inf.biopepa.core.dom.VariableDeclaration;

/**
 * @author Mirco
 * 
 */
public abstract class AbstractDefinitionCompiler {

	protected VariableDeclaration dec;

	protected ModelCompiler compiler;

	protected VariableDeclaration.Kind kind;

	public AbstractDefinitionCompiler(ModelCompiler compiler, VariableDeclaration.Kind kind, VariableDeclaration dec) {
		if (compiler == null || dec == null || dec.getKind() != kind)
			throw new IllegalArgumentException();
		this.dec = dec;
		this.compiler = compiler;
	}

	final Data getData() throws BioPEPAException {
		// perform run-time checks
		String name = dec.getName().getIdentifier();
		if (name == null)
			throw new IllegalArgumentException("Declaration does not contain left hand side");
		if (kind == VariableDeclaration.Kind.CONTAINER || kind == VariableDeclaration.Kind.SPECIES) {
			if (!(dec.getRightHandSide() instanceof PropertyInitialiser)) {
				throw new IllegalArgumentException("Expected a PropertyInitialiser");
			}
		}
		if (hasDuplicates(name)) {
			ProblemKind pKind = ProblemKind.DUPLICATE_USAGE;
			pKind.setMessage("Duplicate usage of the name: " + name);
			compiler.problemRequestor.accept(pKind, dec);
			throw new CompilerException();
		}

		return doGetData();
	}

	protected boolean hasDuplicates(String name) {
		return compiler.containsAnyDeclaration(name);
	}

	protected abstract Data doGetData() throws BioPEPAException;

}
