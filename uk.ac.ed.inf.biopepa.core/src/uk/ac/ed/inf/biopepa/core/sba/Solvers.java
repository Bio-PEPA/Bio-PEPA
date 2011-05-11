/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.sba;

import java.util.*;

import uk.ac.ed.inf.biopepa.core.interfaces.Solver;

public class Solvers {

	static private List<Class<? extends Solver>> solverList = new ArrayList<Class<? extends Solver>>();
	static private List<String> shortNames = new ArrayList<String>(), longNames = new ArrayList<String>();

	static {
		// Add new ISolvers here
		Solver solver = new ISBJava.Gillespie();
		add(solver.getClass(), solver.getShortName(), solver.getDescriptiveName());
		solver = new ISBJava.GibsonBruck();
		add(solver.getClass(), solver.getShortName(), solver.getDescriptiveName());
		solver = new ISBJava.TauLeap();
		add(solver.getClass(), solver.getShortName(), solver.getDescriptiveName());
		solver = new ISBJava.DormandPrince();
		add(solver.getClass(), solver.getShortName(), solver.getDescriptiveName());
		solver = new ISBJava.IMEX();
		add(solver.getClass(), solver.getShortName(), solver.getDescriptiveName());
		// My new native ODE solver
		solver = new NativeRungaKutta();
		add(solver.getClass(), solver.getShortName(), solver.getDescriptiveName());
	}

	public static String[] getSolverList() {
		return longNames.toArray(new String[] {});
	}
	public static String[] getSolverShortNameList(){
		return shortNames.toArray(new String[]{});
	}

	public static Solver getSolverInstance(String name) {
		if (name == null || name == "") {
			// throw new IllegalArgumentException();
			return null;
		}
		int i = longNames.indexOf(name);
		if (i == -1)
			i = shortNames.indexOf(name);
		if (i == -1)
			return null;
		try {
			return solverList.get(i).newInstance();
		} catch (Exception e) {
			return null;
		}
	}

	private static void add(Class<? extends Solver> solver, String shortName, String longName) {
		if (solver == null || shortName == null || longName == null)
			throw new NullPointerException();
		if (shortName == "" || longName == "")
			throw new IllegalArgumentException();
		if (shortNames.contains(shortName) || longNames.contains(longName))
			throw new IllegalArgumentException();
		solverList.add(solver);
		shortNames.add(shortName);
		longNames.add(longName);
	}
}
