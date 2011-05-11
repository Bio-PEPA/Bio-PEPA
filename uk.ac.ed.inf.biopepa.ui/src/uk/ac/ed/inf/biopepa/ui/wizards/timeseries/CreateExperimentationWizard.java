package uk.ac.ed.inf.biopepa.ui.wizards.timeseries;

import james.core.experiments.BaseExperiment;
import james.core.experiments.variables.ExperimentVariable;
import james.core.experiments.variables.ExperimentVariables;
import james.core.experiments.variables.NoNextVariableException;
import james.core.experiments.variables.modifier.IVariableModifier;
import james.core.experiments.variables.modifier.VariableModifier;

import java.util.LinkedList;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Button;

import uk.ac.ed.inf.biopepa.core.sba.ExperimentSet;
import uk.ac.ed.inf.biopepa.core.sba.LineStringBuilder;
import uk.ac.ed.inf.biopepa.core.sba.SBAReaction;
import uk.ac.ed.inf.biopepa.core.sba.ExperimentLine;
import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;
import uk.ac.ed.inf.biopepa.ui.interfaces.IResourceProvider;
import uk.ac.ed.inf.biopepa.ui.wizards.timeseries.AbstractExperimentPage.ArrayInput;

public class CreateExperimentationWizard extends Wizard implements
		IResourceProvider {

	private static final ExperimentVariable<Double> DoubleVectorElementModifier = null;
	BioPEPAModel model;
	private ExperimentSet experimentSet;
	private BaseExperiment baseExperiment;
	
	public CreateExperimentationWizard(BioPEPAModel model) {
		if (model == null)
			throw new NullPointerException("Error; model does not exist.");
		this.model = model;
		setHelpAvailable(false);
		setWindowTitle("Create Experimentation .csv");
	}
	
	public ExperimentSet getExperimentSet (){
		return this.experimentSet;
	}
	
	private ReactionKnockoutPage reactionKnockoutPage;
	private InitPopsExperimentPage initPopsExperPage ;
	private RateVariablesExperimentPage rateVarExperPage;
	
	public void addPages() {
		LineStringBuilder sb = new LineStringBuilder();
		sb.appendLine("For each reaction which is selected a single experiment of two ");
		sb.appendLine("lines will be performed. The first line in which the reaction is ");
		sb.appendLine("enabled and the second line in which the reaction is disabled. ");
		sb.appendLine("If you wish to range over only rate variables or initial component ");
		sb.appendLine("populations then just leave this page blank.");
		reactionKnockoutPage = new ReactionKnockoutPage(model) ;
		reactionKnockoutPage.setHeaderHelp(sb.toString());
		reactionKnockoutPage.setDefaultSelection(false);
		addPage (reactionKnockoutPage) ;
		initPopsExperPage = new InitPopsExperimentPage(model) ;
		addPage (initPopsExperPage) ;
		rateVarExperPage = new RateVariablesExperimentPage(model);
		addPage (rateVarExperPage);
	}
	
	private class NumberArrayVariableModifier extends VariableModifier<Number> implements IVariableModifier<Number>{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private Number[] numbers;
		private int counter = 0;
		public NumberArrayVariableModifier (Number[] numbers){
			this.counter = 0;
			this.numbers = numbers;
		}
		
		@Override
		  public Number next(ExperimentVariables variables)
		      throws NoNextVariableException {
		    int currentIndex = counter;
		    if (currentIndex == numbers.length)
		      throw new NoNextVariableException();
		    counter++;
		    return this.numbers[currentIndex];
		  }

		  @Override
		  public void reset() {
		    counter = 0;
		  }
	}
	
	private void computeBaseExperiment (){
		LinkedList<ArrayInput> concentrationArrays = initPopsExperPage.getArrayInputs();
		ExperimentVariables topLevel = null;
		
		// First do the concentration arrays
		for (ArrayInput arrayInput : concentrationArrays){
			ExperimentVariables thisLevel = new ExperimentVariables();
			// If this is the first level created then set the top level
			// to this.
			if (topLevel == null){ topLevel = thisLevel; }
			NumberArrayVariableModifier arrayModifier = new NumberArrayVariableModifier(arrayInput.getDoubleValues()); 
			ExperimentVariable<Number> variable = 
				new ExperimentVariable<Number>(arrayInput.getName(), 0, arrayModifier);
			thisLevel.addVariable(variable);
		}
		
		// Create the base experiment and add in the experiment variables:
		BaseExperiment baseExperiment = new BaseExperiment();
		baseExperiment.setExperimentVariables(topLevel);
		this.baseExperiment = baseExperiment;
	}
	
	public void computeExperimentSet(){
	
	
		ExperimentSet experimentation = new ExperimentSet ();
		/* The initial populations page*/
		initPopsExperPage.addExperimentArrays(experimentation);
		/* The reactions knockout page */
		// reactionKnockoutPage.addKnockOutExperiments(experimentation);
		LinkedList<SBAReaction> selectedReactions =
			reactionKnockoutPage.getSelectedReactions();
		for(SBAReaction reaction : selectedReactions){
			String reactionName = reaction.getName();
			// Add the off line
			String lineName     = reactionName + "-off";
			ExperimentLine experLineOff = new ExperimentLine (lineName);
			experLineOff.addReactionActivation(reactionName, false);
			experimentation.addExperimentLine(experLineOff);
			// Add the on line
			lineName = reactionName + "-on";
			ExperimentLine experLineOn = new ExperimentLine(lineName);
			experLineOn.addReactionActivation(reactionName, true);
			experimentation.addExperimentLine(experLineOn);
		}
				
		/* Finally the rate experimentation page */
		rateVarExperPage.addExperimentArrays(experimentation);
		
		
		// Do the same for the reactions page
		// build a rates page and do the same for that
		// allow turning an experimentset into a string
		// build a saveas dialog and run that.
		this.experimentSet = experimentation;
	}
	
	@Override
	public boolean performFinish() {
		computeExperimentSet();
		computeBaseExperiment();
		return true;
	}

	public IResource getUnderlyingResource() {
		// TODO Auto-generated method stub
		return null;
	}

}
