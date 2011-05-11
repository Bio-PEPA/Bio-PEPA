/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom.internal;

import uk.ac.ed.inf.biopepa.core.dom.IBinding;
import uk.ac.ed.inf.biopepa.core.dom.IBindingResolver;
import uk.ac.ed.inf.biopepa.core.dom.Model;
import uk.ac.ed.inf.biopepa.core.dom.Statement;
import uk.ac.ed.inf.biopepa.core.dom.VariableDeclaration;

/**
 * Naive binding resolver
 * 
 * @author mtribast
 * 
 */
public class BindingResolver implements IBindingResolver {

	private Model model;

	public BindingResolver(Model newModel) {
		if (newModel == null)
			throw new IllegalArgumentException();
		this.model = newModel;
	}

	public IBinding resolveName(String identifier) {
		for (Statement s : model.statements()) {
			if (s instanceof VariableDeclaration) {
				final VariableDeclaration dec = (VariableDeclaration) s;

				if (dec.getKind() != VariableDeclaration.Kind.SPECIES) {
					if (dec.getName().getIdentifier().equals(identifier)) {
						return new IBinding() {

							public VariableDeclaration getVariableDeclaration() {
								return dec;
							}
						};
					}
				}
			}
		}
		return null;
	}

}
