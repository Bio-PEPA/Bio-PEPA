/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import uk.ac.ed.inf.biopepa.core.dom.ISourceRange;

/**
 * Default is reporting an error.
 * 
 * @author ajduguid
 * 
 */
public class ProblemInfo {

	public enum Severity {
		ERROR, WARNING, INFO;
	}

	// ProblemKind kind;

	public String message;

	public ISourceRange sourceRange;

	public Severity severity;

	public ProblemInfo() {
		severity = Severity.ERROR;
	}

	@Deprecated
	public ProblemInfo(ProblemKind kind, String message, ISourceRange sourceRange) {
		this();
		// this.kind = kind;
		this.message = message;
		this.sourceRange = sourceRange;
	}

	public ProblemInfo(String message, ISourceRange sourceRange) {
		this();
		this.message = message;
		this.sourceRange = sourceRange;
	}

	@Deprecated
	public ProblemInfo(ProblemKind kind, String message, Data data) {
		this();
		// this.kind = kind;
		this.message = message;
		this.sourceRange = data.declaration.getSourceRange();
	}

	public ProblemInfo(String message, Data data) {
		this();
		this.message = message;
		this.sourceRange = data.declaration.getSourceRange();
	}

	public ProblemInfo(String message, PrefixData data) {
		this();
		this.message = message;
		this.sourceRange = data.declaration.getSourceRange();
	}
}
