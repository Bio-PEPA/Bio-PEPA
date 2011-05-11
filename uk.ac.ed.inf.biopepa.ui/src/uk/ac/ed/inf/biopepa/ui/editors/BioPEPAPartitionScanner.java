/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.editors;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class BioPEPAPartitionScanner extends RuleBasedPartitionScanner {
	public final static String COMMENT = "__comment";

	public BioPEPAPartitionScanner() {
		IToken comment = new Token(COMMENT);
		IPredicateRule[] rules = new IPredicateRule[2];
		rules[0] = new MultiLineRule("/*", "*/", comment);
		rules[1] = new EndOfLineRule("//", comment);
		setPredicateRules(rules);
	}
}
