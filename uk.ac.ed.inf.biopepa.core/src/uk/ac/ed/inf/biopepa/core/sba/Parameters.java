/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.sba;

import java.lang.reflect.Constructor;
import java.util.*;

public class Parameters {

	Map<Parameter, Object> parameters = new HashMap<Parameter, Object>();
    public Map<Parameter, Object> getParameters(){
    	return this.parameters;
    }
	
	public enum Parameter {
		Start_Time("Start time", "start-time", Double.class, 0.00), Stop_Time("Stop time", "stop-time", Double.class,
				100.0), Step_Size("Step size", "step-size", Double.class, 0.001), Data_Points("Number of data points",
				"data-points", Integer.class, 100), Components("Components", "components",
				(new String[] {}).getClass(), new String[] {}), Independent_Replications(
				"Number of independent replications", "replications", Integer.class, 1), Relative_Error(
				"Relative error", "relative-error", Double.class, 0.0001), Absolute_Error("Absolute error",
				"absolute-error", Double.class, 0.0001), Confidence_Interval("Confidence interval",
				"confidence-interval", Double.class, 0.05);

		Class<?> parameterClass;
		String argument, descriptiveName;
		Object defaultValue;

		Parameter(String name, String argument, Class<?> parameterClass, Object defaultValue) {
			this.descriptiveName = name;
			this.argument = argument;
			this.parameterClass = parameterClass;
			this.defaultValue = defaultValue;
		}

		public Object getDefault() {
			return defaultValue;
		}

		public String getDescriptiveName(){
			return this.descriptiveName;
		}
		public String toString() {
			return descriptiveName;
		}

		public String getKey() {
			return argument;
		}

		public Class<? extends Object> getType() {
			return parameterClass;
		}

		public Object parseString(String value) {
			try {
				return parameterClass.getConstructor(String.class).newInstance(value);
			} catch (Exception e) {
				return null;
			}
		}
	}

	public Object getValue(Parameter parameter) {
		return parameters.get(parameter);
	}

	public void setValue(Parameter parameter, Object value) {
		if (parameter.parameterClass.isInstance(value))
			parameters.put(parameter, value);
		else if (parameter.equals(Parameter.Components) && value instanceof String) {
			StringBuilder sb = new StringBuilder((String) value);
			ArrayList<String> al = new ArrayList<String>();
			int index, length;
			try {
				while (sb.length() > 0) {
					index = sb.indexOf(":");
					length = Integer.parseInt(sb.substring(0, index));
					sb.delete(0, index + 1);
					al.add(sb.substring(0, length));
					sb.delete(0, length);
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(parameter.descriptiveName + " requires a legal bencoded string.");
			}
			parameters.put(parameter, al.toArray(new String[] {}));
		} else if (value instanceof String) { // try and cast...
			try {
				Constructor<?> constructor = parameter.parameterClass.getConstructor(String.class);
				Object o = constructor.newInstance(value);
				parameters.put(parameter, o);
			} catch (Exception e) {
				throw new IllegalArgumentException("Value is not of type " + parameter.parameterClass.getName()
						+ " and cannot be constructed using a String as a parameter.");
			}
		} else
			throw new IllegalArgumentException("Value is not of type " + parameter.parameterClass.getName());
	}

	public Parameter[] arrayOfKeys() {
		Parameter[] p = new Parameter[parameters.size()];
		Parameter[] q = Parameter.values();
		int i = 0;
		for (Parameter param : q)
			if (parameters.containsKey(param))
				p[i++] = param;
		return p;
	}

	public Set<Parameter> setOfKeys() {
		return parameters.keySet();
	}

	void add(Parameter parameter) {
		if (parameter == null)
			throw new NullPointerException();
		parameters.put(parameter, parameter.getDefault());
	}
}
