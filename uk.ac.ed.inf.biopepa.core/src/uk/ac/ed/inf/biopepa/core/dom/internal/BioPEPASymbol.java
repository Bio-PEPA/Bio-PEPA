/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.dom.internal;

import java_cup.runtime.Symbol;

/**
 * @author Mirco
 * 
 */
public class BioPEPASymbol extends Symbol {

	int line, column;

	/**
	 * @param sym_num
	 */
	public BioPEPASymbol(int sym_num) {
		super(sym_num);
	}

	/**
	 * @param id
	 * @param o
	 */
	public BioPEPASymbol(int id, Object o) {
		super(id, o);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param id
	 * @param left
	 * @param right
	 */
	public BioPEPASymbol(int id, Symbol left, Symbol right) {
		super(id, left, right);
	}

	/**
	 * @param id
	 * @param l
	 * @param r
	 */
	public BioPEPASymbol(int id, int l, int r) {
		super(id, l, r);
	}

	/**
	 * @param id
	 * @param left
	 * @param right
	 * @param o
	 */
	public BioPEPASymbol(int id, Symbol left, Symbol right, Object o) {
		super(id, left, right, o);
	}

	/**
	 * @param id
	 * @param l
	 * @param r
	 * @param o
	 */
	public BioPEPASymbol(int id, int l, int r, Object o) {
		super(id, l, r, o);
	}

}
