/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.analysis;

import java.util.List;

import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.compiler.ProblemInfo;
import uk.ac.ed.inf.biopepa.core.dom.Model;

public class StaticAnalysis {

	public synchronized static List<ProblemInfo> analysis(Model model, ModelCompiler compiledModel) {
		List<ProblemInfo> problems = PredefinedLaws.checkPredefinedLaws(model, compiledModel);
		problems.addAll(ActionCooperationAnalysis.checkActions(compiledModel));
		problems.addAll(ReactantRateParticipationCheck.checkActions(compiledModel));
		return problems;
	}

}
