/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;
import org.eclipse.swt.graphics.RGB;

import uk.ac.ed.inf.biopepa.ui.BioPEPAPlugin;

public class BioPEPAScanner extends RuleBasedScanner {

	public BioPEPAScanner() {
		ColourManager cm = BioPEPAPlugin.getDefault().getColourManager();
		List<IRule> rules = new ArrayList<IRule>();
		// Rule 1
		Token token = new Token(new TextAttribute(cm
				.getColor(ColourManager.DEFAULT)));
		WordRule wr = new WordRule(new IWordDetector() {
			public boolean isWordPart(char c) {
				return c == '@';
			}

			public boolean isWordStart(char c) {
				return isWordPart(c);
			}
		}, token);
		token = new Token(new TextAttribute(cm.getColor(ColourManager.LOCATED)));
		wr.addWord("@", token);
		rules.add(wr);
		token = new Token(new TextAttribute(cm.getColor(ColourManager.DEFAULT)));
		wr = new WordRule(new IWordDetector() {
			public boolean isWordPart(char c) {
				return Character.isJavaIdentifierPart(c);
			}

			public boolean isWordStart(char c) {
				return Character.isLetter(c);
			}
		}, token);
		token = new Token(new TextAttribute(cm.getColor(new RGB(0, 200, 0))));
		token = new Token(new TextAttribute(cm
				.getColor(ColourManager.LOCATION_1)));
		wr.addWord("location", token);
		token = new Token(new TextAttribute(cm
				.getColor(ColourManager.LOCATION_2)));
		wr.addWord("size", token);
		wr.addWord("type", token);
		token = new Token(new TextAttribute(cm.getColor(ColourManager.LOCATED)));
		wr.addWord("in", token);
		// wr.addWord("species", token);
		token = new Token(new TextAttribute(cm
				.getColor(ColourManager.KINETIC_1)));
		wr.addWord("kineticLawOf", token);
		token = new Token(new TextAttribute(cm
				.getColor(ColourManager.KINETIC_2)));
		wr.addWord("fMA", token);
		wr.addWord("fMM", token);

		rules.add(wr);
		token = new Token(new TextAttribute(cm.getColor(ColourManager.DEFAULT)));
		wr = new WordRule(new IWordDetector() {
			public boolean isWordPart(char c) {
				return c == '>' || c == '<' || c == '+' || c == '-' || c == '.'
						|| c == ')';
			}

			public boolean isWordStart(char c) {
				return c == '>' || c == '<' || c == '(' || c == ')' || c == '-';
			}
		}, token);
		token = new Token(new TextAttribute(cm.getColor(ColourManager.OP)));
		wr.addWord(">>", token);
		wr.addWord("<<", token);
		wr.addWord("(+)", token);
		wr.addWord("(-)", token);
		wr.addWord("(.)", token);
		wr.addWord("->", token);
		wr.addWord("<->", token);
		rules.add(wr);
		// Rule 2
		WhitespaceRule wsr = new WhitespaceRule(new IWhitespaceDetector() {
			public boolean isWhitespace(char c) {
				return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
			}
		});
		rules.add(wsr);
		setRules(rules.toArray(new IRule[] {}));
		setDefaultReturnToken(new Token(new TextAttribute(cm
				.getColor(ColourManager.DEFAULT))));
	}

}
