/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.*;

public class DynamicComponentGathererVisitor extends DefaultCompilerVisitor {

	protected Set<String> components = new HashSet<String>();

	public DynamicComponentGathererVisitor(ModelCompiler compiler) {
		super(compiler);
	}

	public boolean visit(Name name) throws BioPEPAException {
		return true;
	}

	public boolean visit(Component component) throws BioPEPAException {
		components.add(component.getName().getIdentifier());
		return true;
	}

	public boolean visit(Cooperation cooperation) throws BioPEPAException {
		cooperation.getLeftHandSide().accept(this);
		cooperation.getRightHandSide().accept(this);
		return true;
	}

}
