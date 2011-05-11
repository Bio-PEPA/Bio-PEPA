/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

/**
 * 
 * @author ajduguid
 * 
 */
public class LocatedName extends Name {

	private NameSet locations;

	LocatedName(AST ast) {
		super(ast);
	}

	public void setLocations(NameSet locations) {
		this.locations = locations;
	}

	public NameSet getLocations() {
		return locations;
	}

	public String getName() {
		return super.getIdentifier();
	}

	public String getIdentifier() {
		if (locations.names().size() > 1)
			throw new UnsupportedOperationException(
					"Cannot produce a unique identifier where definition applies to more than one Location");
		return super.getIdentifier() + AST.Literals.LOCATION + locations.names().get(0).getIdentifier();
	}

	public String getIdentifier(int index) {
		return super.getIdentifier() + AST.Literals.LOCATION + locations.names().get(index).getIdentifier();
	}

	/**
	 * Gets the binding for this name
	 * 
	 * @return the binding for this name, or null
	 */
	public IBinding getBinding() {
		return ast.getBindingResolver().resolveName(getIdentifier());
	}

}
