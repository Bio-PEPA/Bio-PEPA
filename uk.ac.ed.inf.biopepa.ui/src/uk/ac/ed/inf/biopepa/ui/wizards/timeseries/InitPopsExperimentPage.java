package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentSet;
import uk.ac.ed.inf.biopepa.core.sba.LineStringBuilder;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentLine;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;

public class InitPopsExperimentPage extends AbstractExperimentPage {
	public final static String wizardPageName = "Initial Concentration Setup Page";

	// private BioPEPAModel model;

	public InitPopsExperimentPage(BioPEPAModel model) {
		super(wizardPageName);
		// this.model = model;
		setTitle("Initial Concentration Setup and Experimentation Page");
		setDescription("Set up experiments over component initial populations ");
		
		LineStringBuilder sb = new LineStringBuilder();
		
		sb.append("For each component that you wish to range over ");
		sb.appendLine("either check the left box and");
		sb.append("enter a comma ");
		sb.appendLine("separated list of double values or check the right ");
		sb.append("box and enter a range via start and stop values with ");
		sb.appendLine("a step size.");
		sb.appendLine("Any component with unchecked boxes will not be ranged ");
		sb.appendLine("over in this experiment and their default values used.");
		this.setHeader(sb.toString());

		/* Now we must set up the experiment object names */
		ComponentNode[] species = model.getSBAModel().getComponents();
		this.experimentObjectNameHints = new NameHintPair[species.length];
		for (int i = 0; i < species.length; i++) {
			ComponentNode compNode = species[i];
			String name = compNode.getName();
			String hint = Long.toString(compNode.getCount());
			NameHintPair nameHint = new NameHintPair(name, hint);
			experimentObjectNameHints[i] = nameHint;
		}
	}

	@Override
	public void addExperimentArrays(ExperimentSet experiment) {
		for (ArrayInput arrayInput : arrayInputs) {
			String compName = arrayInput.getName();
			Number[] values = arrayInput.getDoubleValues();
			for (Number value : values) {
				ExperimentLine eline = new ExperimentLine(compName
						+ "-" + value);
				eline.addInitialConcentration(compName, value);
				experiment.addExperimentLine(eline);
			}
		}

	}

}
