package uk.ac.ed.inf.biopepa.core.sba;

import java.util.HashMap;
import java.util.Map;

public class Experimentation {
	private Map<String, Number[]> initialArrays;

	public Experimentation() {
		initialArrays = new HashMap<String, Number[]>();
	}

	public void addInitialArray(String componentName, Number[] values) {
		initialArrays.put(componentName, values);
	}

	public Map<String, Number[]> getInitialArrays() {
		return initialArrays;
	}
}
