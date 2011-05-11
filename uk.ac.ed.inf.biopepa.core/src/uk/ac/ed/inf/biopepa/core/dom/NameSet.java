/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import java.util.List;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * Used for either actions or lists of locations.
 * 
 * @author ajduguid
 * 
 */
public class NameSet extends ASTNode {

	ASTNode.NodeList<Name> names = new ASTNode.NodeList<Name>();

	NameSet(AST ast) {
		super(ast);
	}

	@Override
	public void accept(ASTVisitor visitor) throws BioPEPAException {
		visitor.visit(this);
	}

	/**
	 * Returns the list of actions of this action set
	 * 
	 * @return
	 */
	public List<Name> names() {
		return names;
	}

}
