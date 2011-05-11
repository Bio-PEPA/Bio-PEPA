/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.tests;

import java.io.*;

import java_cup.runtime.Symbol;
import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.compiler.ProblemInfo;
import uk.ac.ed.inf.biopepa.core.dom.Model;
import uk.ac.ed.inf.biopepa.core.dom.internal.BioPEPALexer;
import uk.ac.ed.inf.biopepa.core.dom.internal.BioPEPAParser;
import uk.ac.ed.inf.biopepa.core.dom.internal.BioPEPASymbolFactory;
import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.interfaces.Solver;
import uk.ac.ed.inf.biopepa.core.sba.*;
import uk.ac.ed.inf.biopepa.core.sba.Parameters.Parameter;

public class Parser {

	public static void main(String[] args) throws Exception {
		String source = null;
		if (args.length == 0) {
			System.err.println("Please specify input file:");
			System.err.println(" java -jar biopepa.jar <filename.biopepa>");
			System.exit(1);
		} else {
			source = readFile(args[0]);
		}
		long tic = System.currentTimeMillis();
		Model model = parse(source);
		System.out.println("Preview of BioPEPA Compiler 0.0.1.v20080208");
		System.out.println("*******************************************\n");
		ModelCompiler c = new uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler(model);
		ProblemInfo[] problems = c.compile();
		long toc = System.currentTimeMillis();

		if (problems.length == 0) {
			System.out.println("Model accepted.");
			System.out.println("Elapsed time:" + (toc - tic) + " ms");
			SBAModel sba = new SBAModel(c);
			sba.parseBioPEPA();
			System.out.println(sba.toString());
			System.out.println("--------------------------------------------------------------------------------");
			Solver solver = Solvers.getSolverInstance("gillespie");
			Parameters parameters = solver.getRequiredParameters();
			parameters.setValue(Parameter.Stop_Time, 20.0);
			parameters.setValue(Parameter.Independent_Replications, 1000);
			parameters.setValue(Parameter.Components, new String[] { "X", "Y", "Z" });
			Result result = solver.startTimeSeriesAnalysis(sba, parameters, null);
			String[] components = result.getComponentNames();
			System.out.println("Final population levels");
			for (int i = 0; i < components.length; i++)
				System.out.println(components[i] + " = " + result.getPopulation(i));
		} else {
			System.out.println("Compiler returned with the following warnings/errors:");
			System.out.println();
			for (ProblemInfo p : problems) {
				System.out.print(p.message);
				if (p.sourceRange == null)
					System.out.println(" [?]");
				else
					System.out.println(" [" + p.sourceRange.getChar() + "]");
			}
		}
		System.exit(0);

	}

	private static String readFile(String location) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(location));
		StringBuffer buffer = new StringBuffer();
		String line = null;
		while ((line = r.readLine()) != null) {
			buffer.append(line + "\n");
		}
		return buffer.toString();
	}

	public static Model parse(String source) throws Exception {
		// ComplexSymbolFactory symbolFactory = new ComplexSymbolFactory();
		BioPEPASymbolFactory symbolFactory = new BioPEPASymbolFactory();
		BioPEPAParser parser = new BioPEPAParser(new BioPEPALexer(new StringReader(source), symbolFactory),
				symbolFactory);
		Symbol symbol = parser.parse();
		return (Model) symbol.value;

	}
}
