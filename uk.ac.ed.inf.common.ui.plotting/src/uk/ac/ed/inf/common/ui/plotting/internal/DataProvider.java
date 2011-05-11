/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.ui.plotting.internal;

import java.util.HashMap;

public class DataProvider {
	
	private HashMap<String, Object[]> data =
		new HashMap<String, Object[]>();
		
	public void provide(String key, Object[] objects) {
		if (key == null)
			throw new NullPointerException();
		if (data.containsKey(key))
			throw new IllegalStateException();
		data.put(key, objects);
	}
	
	public Object[] getObjects(String key) {
		return data.get(key);
	}

}
