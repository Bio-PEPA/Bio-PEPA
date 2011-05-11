/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import java.util.ArrayList;
import java.util.List;

public class ActionData extends PrefixData {

	List<String> locations = new ArrayList<String>();

	void addLocation(String location) {
		locations.add(location);
	}

	void setLocations(List<String> locations) {
		this.locations = locations;
	}

	public List<String> getLocations() {
		List<String> l = new ArrayList<String>(locations.size());
		for (String s : locations)
			l.add(s);
		return l;
	}
}
