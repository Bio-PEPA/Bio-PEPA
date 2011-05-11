/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

public abstract class CompiledExpression implements Cloneable {

	CompiledExpression expandedForm = null;

	private boolean isTheExpandedForm = false;

	public abstract boolean accept(CompiledExpressionVisitor visitor);

	public abstract CompiledExpression clone();
	
	public abstract boolean isDynamic ();

	public boolean hasExpandedForm() {
		return expandedForm != null;
	}

	public CompiledExpression returnExpandedForm() {
		return expandedForm;
	}

	public CompiledExpression returnExpandedIfPresent(){
		if (this.expandedForm != null){
			return this.expandedForm;
		} else {
			return this;
		}
	}
	
	void setExpandedForm(CompiledExpression expandedForm) {
		if (isTheExpandedForm)
			throw new IllegalStateException();
		expandedForm.setAsExpandedForm();
		this.expandedForm = expandedForm;
	}

	/*
	 * Hi Adam? I'm not sure why originally an expanded form
	 * was not allowed to also have an expanded form??
	 * I've removed this restriction because it is useful for
	 * changing rate variables via experimentation. For example
	 * we want to compile:
	 * a = 3;
	 * r = [ a ] ;
	 * The [a] expression as a static variable 'a' with expanded
	 * form '3' this allows us to know both that the expression
	 * evaluates to '3' and that it is actually 'a' so that if we
	 * have overridden the value of 'a' in an experimentation line
	 * then we know to ignore the '3' and use the overriding value.
	 */
	void setAsExpandedForm() {
		// expandedForm = null;
		isTheExpandedForm = true;
	}
}
