/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;

import java.util.HashSet;

/**
 * @author Mirco
 * 
 */
public class DefaultProblemPolicy implements IProblemPolicy {

	private static final HashSet<ProblemKind> WARNINGS = new HashSet<ProblemKind>();

	static {
		WARNINGS.add(ProblemKind.DUPLICATE_ACTION_FOUND);
		WARNINGS.add(ProblemKind.DEFINITION_DECLARED_BUT_NOT_USED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.compiler.IProblemPolicy#isWarning(uk.ac.ed.
	 * inf.biopepa.core.compiler.ProblemKind)
	 */
	public boolean isWarning(ProblemKind kind) {
		return WARNINGS.contains(kind);
	}

}
