/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.compiler;


public abstract class CompiledExpressionVisitor {

	public abstract boolean visit(CompiledDynamicComponent component);
	
	public abstract boolean visit(CompiledFunction function);

	public abstract boolean visit(CompiledNumber number);

	public abstract boolean visit(CompiledOperatorNode operator);

	public abstract boolean visit(CompiledSystemVariable variable);

}
