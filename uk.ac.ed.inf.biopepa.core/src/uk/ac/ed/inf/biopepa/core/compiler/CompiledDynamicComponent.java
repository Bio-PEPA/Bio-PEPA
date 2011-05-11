/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

public class CompiledDynamicComponent extends CompiledExpression {

	String name;

	public CompiledDynamicComponent(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}

	public boolean accept(CompiledExpressionVisitor visitor) {
		return visitor.visit(this);
	}
	
	public boolean isDynamic (){
		return true;
	}

	public CompiledDynamicComponent clone() {
		CompiledDynamicComponent cdc = new CompiledDynamicComponent(name);
		if (expandedForm != null)
			cdc.expandedForm = expandedForm.clone();
		return cdc;
	}
}
