/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.dom.ASTNode;

/**
 * @author mtribast
 * 
 */
public class Data implements Comparable<Object> {

	protected String name;

	protected int usages;

	ASTNode declaration;

	protected Data(String name, ASTNode definingDeclaration) {
		if (name == null)
			throw new IllegalArgumentException();
		this.name = name;
		this.declaration = definingDeclaration;
		this.usages = 0;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		return name.equals(o);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	void registerNewUsage() {
		usages++;
	}

	public int getUsage() {
		return usages;
	}

	public int compareTo(Object o) {
		return name.compareTo(((Data) o).name);
	}

}
