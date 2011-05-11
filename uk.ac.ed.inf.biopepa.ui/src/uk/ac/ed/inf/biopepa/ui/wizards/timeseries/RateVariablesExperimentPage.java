package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpression;
import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.compiler.VariableData;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentLine;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentSet;
import uk.ac.ed.inf.biopepa.core.sba.LineStringBuilder;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;

public class RateVariablesExperimentPage extends AbstractExperimentPage {
	public final static String wizardPageName = "Rate Variables Setup Page";

	// private BioPEPAModel model;

	public RateVariablesExperimentPage(BioPEPAModel model) {
		super(wizardPageName);
		// this.model = model;
		setTitle("Rate Variables Setup and Experimentation Page");
		setDescription("Set up experiments over rate variables ");
		
		LineStringBuilder sb = new LineStringBuilder();
		
		sb.append("For each variable that you wish to range over ");
		sb.appendLine("either check the left box");
		sb.append("and enter a comma ");
		sb.appendLine("separated list of double values or check the right ");
		sb.append("box and enter a range via start and stop values with ");
		sb.appendLine("a step size.");
		sb.appendLine("Any rate variable with unchecked boxes will not be ranged ");
		sb.appendLine("over in this experiment and their default values used.");
		super.setHeader(sb.toString());

		/* Now we must set up the experiment object names */
		ModelCompiler mCompiler = model.getCompiledModel();
		VariableData[] dynVariableData = mCompiler.getStaticVariables();
		this.experimentObjectNameHints = new NameHintPair[dynVariableData.length];
		int i = 0;
		for (VariableData dynVariable : dynVariableData) {
			String name = dynVariable.getName();
			CompiledExpression value = dynVariable.getValue();
			String hint = value.toString();
			if (hint.length() > 30){
				hint = "expr";
			}
			experimentObjectNameHints[i++] = new NameHintPair(name, hint); 
		}

	}

	@Override
	public void addExperimentArrays(ExperimentSet experiment) {
		for (ArrayInput arrayInput : arrayInputs) {
			String varName = arrayInput.getName();
			Number[] values = arrayInput.getDoubleValues();
			for (Number value : values) {
				ExperimentLine eline = 
					new ExperimentLine(varName + "-" + value);
				eline.addRateValue(varName, value);
				experiment.addExperimentLine(eline);
			}
		}

	}

}
