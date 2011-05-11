/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core;

import java.io.StringReader;

import java_cup.runtime.Symbol;
import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.dom.Model;
import uk.ac.ed.inf.biopepa.core.dom.internal.*;
import uk.ac.ed.inf.biopepa.core.interfaces.Solver;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.Solvers;

public class BioPEPA {

	public static Model parse(String source) throws ParserException, Exception {
		BioPEPASymbolFactory symbolFactory = new BioPEPASymbolFactory();
		BioPEPAParser parser = new BioPEPAParser(new BioPEPALexer(new StringReader(source), symbolFactory),
				symbolFactory);
		try {
			Symbol symbol = parser.parse();
			return (Model) symbol.value;
		} catch (Exception e) {
			if (e instanceof ParserException)
				throw (ParserException) e;
			throw e;
		}
	}

	public static ModelCompiler compile(Model model) {
		return new ModelCompiler(model);
	}

	public static SBAModel generateSBA(ModelCompiler compiledModel) {
		SBAModel model = new SBAModel(compiledModel);
		model.parseBioPEPA();
		return model;
	}

	public static String[] getSolvers() {
		return Solvers.getSolverList();
	}

	public static Solver getSolverInstance(String name) {
		return Solvers.getSolverInstance(name);
	}

}
