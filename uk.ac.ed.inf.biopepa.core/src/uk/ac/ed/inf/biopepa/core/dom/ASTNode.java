/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom;

import java.util.ArrayList;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;

/**
 * Super class of elements of the Abstract Syntax Tree.
 * 
 * @author mtribast
 * 
 */
public abstract class ASTNode {

	@SuppressWarnings("serial")
	class NodeList<T> extends ArrayList<T> {

	};

	protected AST ast;

	protected ISourceRange sourceRange;

	ASTNode(AST ast) {
		if (ast == null)
			throw new IllegalArgumentException();
		this.ast = ast;
	}

	public ISourceRange getSourceRange() {
		return sourceRange;
	}

	public void setSourceRange(final int position, final int length, final int line, final int column) {
		this.sourceRange = new ISourceRange() {

			public int getLength() {
				return length;
			}

			public String toString() {
				return "position:" + position + ",length:" + length;
			}

			public int getColumn() {
				return column;
			}

			public int getChar() {
				return position;
			}

			public int getLine() {
				return line;
			}

		};
	}

	public abstract void accept(ASTVisitor visitor) throws BioPEPAException;

}
