/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

public class CompiledSystemVariable extends CompiledExpression {

	public enum Variable {
		TIME("time");

		String id;

		Variable(String id) {
			this.id = id;
		}

		public String toString() {
			return id;
		}
	}

	Variable variable;

	CompiledSystemVariable(Variable variable) {
		this.variable = variable;
	}

	public boolean accept(CompiledExpressionVisitor visitor) {
		return visitor.visit(this);
	}
	
	public boolean isDynamic (){
		return true;
	}

	public Variable getVariable() {
		return variable;
	}

	public String toString() {
		return variable.toString();
	}

	public CompiledSystemVariable clone() {
		CompiledSystemVariable csv = new CompiledSystemVariable(variable);
		if (expandedForm != null)
			csv.expandedForm = expandedForm.clone();
		return csv;
	}
}
