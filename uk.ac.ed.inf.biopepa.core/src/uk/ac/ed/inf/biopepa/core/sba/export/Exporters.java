/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.sba.export;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ed.inf.biopepa.core.interfaces.Exporter;

public class Exporters {

	static private List<Class<? extends Exporter>> exporterList = new ArrayList<Class<? extends Exporter>>();
	static private List<String> shortNames = new ArrayList<String>(), longNames = new ArrayList<String>();

	static {
		// Add new exporters here
		Exporter sbmle = new SBMLExport();
		add(SBMLExport.class, sbmle.getShortName(), sbmle.getLongName());
		/*
		 * I've decided to remove the traviando export because I think it is not
		 * such a structural export, and may require some significant amount of
		 * time. Moreover it involves actually simulating the model so I think
		 * it is a different class of export. Finally I've found that the 'extra
		 * options' for exporters do not quite work as yet. We require an
		 * interface for exporters which knows about ui elements and the
		 * implementors can therefore draw on the extra options portion of the
		 * export wizard.
		 */
		// Exporter trave = new TraviandoExport ();
		// add(TraviandoExport.class, trave.getShortName(),
		// trave.getLongName());
	}

	public static Exporter getSolverInstance(String name) {
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
			return exporterList.get(i).newInstance();
		} catch (Exception e) {
			return null;
		}
	}

	private static void add(Class<? extends Exporter> solver, String shortName, String longName) {
		if (solver == null || shortName == null || longName == null)
			throw new NullPointerException();
		if (shortName == "" || longName == "")
			throw new IllegalArgumentException();
		if (shortNames.contains(shortName) || longNames.contains(longName))
			throw new IllegalArgumentException();
		exporterList.add(solver);
		shortNames.add(shortName);
		longNames.add(longName);
	}

	public static String[] getShortNames() {
		return shortNames.toArray(new String[] {});
	}

	public static String[] getLongNames() {
		return longNames.toArray(new String[] {});
	}
}
