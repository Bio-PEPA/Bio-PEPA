/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.dom.ASTNode;

/**
 * Definition of a component
 * 
 * @author Mirco
 * 
 */
public class ComponentData extends Data {

	private PrefixData[] prefixes;

	protected ComponentData(String name, ASTNode declaration) {
		super(name, declaration);
	}

	void setPrefixes(PrefixData[] prefixes) {
		this.prefixes = prefixes;
	}

	public PrefixData[] getPrefixes() {
		return prefixes;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[Component] Name=");
		buf.append(getName());
		buf.append(",NumOfPrefixes=");
		buf.append(prefixes.length);
		return buf.toString();
	}

}
