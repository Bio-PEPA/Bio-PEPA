/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom.internal;

import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory;
import uk.ac.ed.inf.biopepa.core.dom.ASTNode;

/**
 * @author Mirco
 * 
 */
public class BioPEPASymbolFactory implements SymbolFactory {

	public Symbol newLocationAwareSymbol(String name, int id, Object value, int startPosition, int endPosition,
			int line, int column) {
		BioPEPASymbol symbol = new BioPEPASymbol(id, value);
		symbol.left = startPosition;
		symbol.right = endPosition;
		symbol.line = line;
		symbol.column = column;
		return symbol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java_cup.runtime.SymbolFactory#newSymbol(java.lang.String, int)
	 */
	public Symbol newSymbol(String name, int id) {
		return newSymbol(name, id, null, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java_cup.runtime.SymbolFactory#newSymbol(java.lang.String, int,
	 * java.lang.Object)
	 */
	public Symbol newSymbol(String name, int id, Object value) {
		return newSymbol(name, id, null, null, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java_cup.runtime.SymbolFactory#newSymbol(java.lang.String, int,
	 * java_cup.runtime.Symbol, java_cup.runtime.Symbol)
	 */
	public Symbol newSymbol(String name, int id, Symbol left, Symbol right) {
		return newSymbol(name, id, left, right, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java_cup.runtime.SymbolFactory#newSymbol(java.lang.String, int,
	 * java_cup.runtime.Symbol, java_cup.runtime.Symbol, java.lang.Object)
	 */
	public Symbol newSymbol(String name, int id, Symbol left, Symbol right, Object value) {
		BioPEPASymbol symbol = new BioPEPASymbol(id);
		symbol.value = value;
		if (left != null) {
			symbol.left = left.left;
			symbol.line = ((BioPEPASymbol) left).line;
		}
		if (right != null)
			symbol.right = right.right;
		if (value != null && value instanceof ASTNode) {
			ASTNode node = (ASTNode) value;
			node.setSourceRange(left.left, right.right - left.left, ((BioPEPASymbol) left).line,
					((BioPEPASymbol) left).column);
		}
		return symbol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java_cup.runtime.SymbolFactory#startSymbol(java.lang.String, int,
	 * int)
	 */
	public Symbol startSymbol(String name, int id, int state) {
		BioPEPASymbol symbol = new BioPEPASymbol(id);
		symbol.parse_state = state;
		symbol.left = 0;
		symbol.right = 0;
		symbol.value = null;
		return symbol;
	}

}
