package uk.ac.ed.inf.biopepa.core.sba.export;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import uk.ac.ed.inf.biopepa.core.compiler.*;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledDynamicComponent;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledExpressionVisitor;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledFunction;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledNumber;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledOperatorNode;
import uk.ac.ed.inf.biopepa.core.compiler.CompiledSystemVariable;
import uk.ac.ed.inf.biopepa.core.compiler.ComponentNode;
import uk.ac.ed.inf.biopepa.core.compiler.ModelCompiler;
import uk.ac.ed.inf.biopepa.core.compiler.VariableData;
import uk.ac.ed.inf.biopepa.core.interfaces.Exporter;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.SBAReaction;
import uk.ac.ed.inf.biopepa.core.sba.StringConsumer;
import uk.ac.ed.inf.biopepa.core.sba.SBAComponentBehaviour.Type;

public class PrismExport implements Exporter {

	public String canExport() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		// Should write more here
		return "Prism is a ctmc model checker";
	}

	public String getExportPrefix() {
		return "prism";
	}

	public String getLongName() {
		return "Prism";
	}

	public String getShortName() {
		return "prism";
	}

	public Object requiredDataStructure() {
		// TODO Auto-generated method stub
		return null;
	}

	private SBAModel sbaModel = null;

	public void setModel(SBAModel model) throws UnsupportedOperationException {
		sbaModel = model;
	}

	private ModelCompiler modelCompiler = null;

	public void setModel(ModelCompiler compiledModel) throws UnsupportedOperationException {
		modelCompiler = compiledModel;
	}

	public void setName(String modelName) {
	}

	public Object toDataStructure() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	private int levelSize = 1;

	public void setLevelSize(int levelSize) {
		this.levelSize = levelSize;
	}

	
	private String prismRename (String comp){
		return "_" + comp;
		// return comp;
	}
	
	/*
	 * We have the problem that Prism doesn't understand all of the
	 * rate expressions that we do, in particular those that deal with
	 * kinetic law functions such as fMA and fMM, so here we must change
	 * it into a compiled expression without the rate law functions.
	 */
	private class PrismRateVisitor extends CompiledExpressionVisitor {
        /*
         * Note here we need not care about the level size, we are
         * calculating here the rate of the reaction for a stepsize or
         * levelsize of one. Since we move the population sizes by the
         * step size the reaction rate is always calculated appropriately.
         * We still have fewer states because fewer configurations are possible.
         * 
         * Once this rate visitor has traversed the rate in question then
         * the caller should divide the given rate by the step size to get
         * the correct rate (otherwise the reaction will take place at the
         * usual rate but have an exagerrated effect).
         * Suppose the reaction should have rate 'r' and move consume one
         * molecule of M and produce one molecule or N. If we are using a
         * step size of 5 then we will consume five molecules of M and produce
         * 5 of N so it needs to happen 5 times less often. 
         */
        private SBAReaction reaction;
		private String value;
        
		PrismRateVisitor (SBAReaction r){
			super();
			this.reaction = r;
		}
		
		public String getValue (){
			return value;
		}
		
		@Override
		public boolean visit(CompiledDynamicComponent component) {
			value = prismRename(component.getName());
			return false;
		}

		@Override
		public boolean visit(CompiledFunction function) {
			if (function.getFunction().isRateLaw()) {
				switch (function.getFunction()) {
				case fMA:
					function.getArguments().get(0).accept(this);
					// String argument = value ;
					List<SBAComponentBehaviour> reactants = reaction.getReactants();
					// TODO: we are not currently supporting reversible
					// reactions.
					// if (reaction.isReversible())
					// reactants = reaction.products;
					
					// if (reactants.size() == 0)
					// break; // constant rate production value already equal to
					// argument.
					// But actually this will work since the 'for' loop will be
					// empty anyway.
					for (SBAComponentBehaviour reactant : reactants) {
						String name = prismRename(reactant.getName());
						int stoich = reactant.getStoichiometry();
						// The expression concerning the current reactant which
						// will either be just the reactant's name if the stoichiometry
						// is 1, or the reactant's name raised to the power of the
						// stoichiometry otherwise.
						String thisExpr;
						if (stoich != 1){
							// thisExpr = "pow(" + name + " * LEVELSIZE, " + stoich + ")";
							thisExpr = "pow(" + name + ", " + stoich + ")";
						} else {
							thisExpr = "( " + name + " )";
						}
						value = value + " * " + thisExpr;
					}
					// We don't require this anymore since the caller cares about
					// the level size.
					// value = "(" + value + ") / LEVELSIZE";
					break;
				
				case fMM:
					function.getArguments().get(0).accept(this);
					String arg1 = value;
					function.getArguments().get(1).accept(this);
					String arg2 = value;
					String substrate = null;
					String enzyme = null;

					// Not 100% sure about this code
					for (SBAComponentBehaviour cb : reaction.getReactants()) {
						if (cb.getType().equals(Type.REACTANT)) {
							substrate = prismRename(cb.getName());
						}
						enzyme = prismRename(cb.getName());
						if (cb.getStoichiometry() != 1) {
							throw new IllegalStateException();
						}
					}
					// Not sure what to do if the stoichiometry is not 1?
					String numerator = "( " + arg1 + " * " + substrate +
					                      " * " + enzyme + " )";
					String denominator = "( " + arg2 + " + " + substrate + ")";
					value = "( " + numerator + " / " + denominator + ")";
					break;
				// TODO Hill kinetics??
				default:
					throw new IllegalStateException();
				}
				return false;
			}
			// If it is not a rate law then we can attempt to
			// interpret it as a normal maths function.
			/* But we do not know how to interpret these as
			 * a compiled expression, so for now these will
			 * have to throw and illegal state exception.
			 */
			if (function.getFunction().args() == 1) {
				function.getArguments().get(0).accept(this);
				String argument = value ;
				// CompiledExpression argument = value ;
				switch (function.getFunction()) {
				case LOG:
					value = "log(" + argument + ", 2.71828183)";
					break;
				case EXP:
					value = "pow(2.71828183, " + argument + ")";
					break;
				case FLOOR:
					value = "floor(" + argument + ")";
					break;
				case CEILING:
					value = "ceil(" + argument + ")";
					break;
				/*
				 * No H or tanh functions in prism so just
				 * fall through and hit the exception.
				case H:
				case TANH:
					break;
				*/
				default:
					throw new IllegalStateException();
				}
				// return false;
			}
			/* If we get here and have not returned then we
			 * we haven't been able to interpret the compiled
			 * expression.
			 */
			throw new IllegalStateException();
		}

		@Override
		public boolean visit(CompiledNumber number) {
			value = number.toString();
			return false;
		}

		@Override
		public boolean visit(CompiledOperatorNode operator) {
			operator.getLeft().accept(this);
			String left = value ;
			operator.getRight().accept(this);
			String right = value ;
			switch (operator.getOperator()) {
			case PLUS:
				value = infixOperatorString (left, "+", right);
				break;
			case MINUS:
				value = infixOperatorString (left, "-", right);
				break;
			case DIVIDE:
				value = infixOperatorString (left, "/", right);
				break;
			case MULTIPLY:
				value = infixOperatorString (left, "*", right);
				break;
			case POWER:
				value = "pow(" + left + ", " + right + ")";
				break;
			default:
				throw new IllegalStateException ();
			}
			return false;
		}
		private String infixOperatorString (String left, String oper, String right){
			String result = "(" + left + ")" + oper + "(" + right + ")"; 
			return result;
		}

		@Override
		public boolean visit(CompiledSystemVariable variable) {
			// TODO find out if we can do 'Time' in Prism?
			throw new IllegalStateException ();
			/*
			switch (variable.getVariable()) {
			
			case TIME:
				value = time ;
				break;
			
			default:
			 throw new IllegalStateException();
			}
			return false;
			*/
		}		
	}
	
	
	/*
	 * This has the problem that I seem to be using both the compiled model and
	 * the sba model, which means it isn't a true 'Exporter' since the export
	 * page attempts to decide which to pass to it. I'm not sure why the export
	 * page doesn't just pass both, perhaps performance? In any case I think it
	 * should be updated to at least allow for that.
	 */
	public void export(StringConsumer sb, StringConsumer ps) throws IOException {
		// LineStringBuilder sb = new LineStringBuilder();

		sb.appendLine("// Prism model compiled from BioPEPA eclipse Plug-in");
		sb.appendLine("// The level size is: " + levelSize);
		Date date = new Date();
		sb.appendLine("// Compiled on: " + date.toString());
		sb.appendLine("");
		sb.appendLine("ctmc");

		// First do the rate constants, relatively easy
		sb.appendLine("");
		sb.appendLine("// Now we deal with the model's constant variables");
		sb.appendLine("");
		VariableData[] staticVars = modelCompiler.getStaticVariables();
		for (VariableData var : staticVars) {
			String name = var.getName();
			String countS = var.getValue().toString();
			sb.appendLine("const double " + name + " = " + countS + ";");
		}

		// Now the rates module
		sb.appendLine("");
		sb.appendLine("// Now we deal with the model's rates");
		sb.appendLine("");
		sb.appendLine("module Rates");
		sb.appendLine("");

		
		// For each reaction we output a line such as:
		// [_r1] (( _k1 * _E * _S ) > 0) -> ( _k1 * _E * _S ) : true;
		// So we are essentially saying that if the rate is greater than
		// zero then its value is the rate. Note that the actual rate
		// will be divided through by the step size, however we don't
		// need to do the division whilst checking for non-zero.
		SBAReaction[] reactions = sbaModel.getReactions();
		for (SBAReaction reaction : reactions) {
			String name = prismRename(reaction.getName());
			PrismRateVisitor prv = new PrismRateVisitor(reaction);
			reaction.getRate().accept(prv);
			String rate = prv.getValue();
			String normRate = "(" + rate + " ) / LEVELSIZE" ;
			
			/*
			if (levelSize != 1){
				rate = "( " + rate + " ) / LEVELSIZE";
			}
			if (levelSize == 1){
				normRate = rate;
			} else {
				normRate = "(" + rate + ") / " + levelSize;
			}
			*/
			sb.append("[");
			sb.append(name);
			sb.append("] ((");
			sb.append(rate);
			sb.append(") > 0 ) -> (");
			sb.append(normRate);
			sb.append(") : true;");
			sb.endLine();
		}
		sb.appendLine("");
		sb.appendLine("endmodule");

		ComponentNode[] components = sbaModel.getComponents();
		String[] componentNames = sbaModel.getComponentNames();
		String[] compCountStrings = new String[componentNames.length];
		for (int i = 0; i < componentNames.length; i++){
			compCountStrings[i] = Long.toString(components[i].getCount());
		}

		// Now the maximum concentration of any species
		// which by the conservation of mass is the sum of
		// all the species' original concentrations
		// Actually that doesn't work at all, as a good
		// guess approximation I add up all the concentrations
		// and multiply them by the largest stoichiometry that
		// I find in the model, so first to find the largest
		// stoichiometry
		int largestStoichiometry = 1 ;
		for (SBAReaction reaction : reactions){
			for (SBAComponentBehaviour product : reaction.getProducts()){
				int  current = product.getStoichiometry();
				largestStoichiometry = Math.max(largestStoichiometry, current);
			}
		}
		sb.appendLine("");
		sb.appendLine("// maximum of concentrations");
		sb.append("// Species: ");
		separateString(componentNames, ", ", sb);
		sb.endLine();
		sb.append("const int MAX = ");
		if (largestStoichiometry == 1){
			separateString(compCountStrings, " + ", sb);
		} else {
			sb.append("(");
			separateString(compCountStrings, " + ", sb);
			sb.append(") * " + largestStoichiometry);
		}
		sb.append(";");
		sb.endLine();
		
		// Set up a constant for the level size
		sb.append("const int LEVELSIZE = ");
		sb.append(Integer.toString(levelSize));
		sb.append(";");
		sb.endLine();

		// Now we must output a module for each component
		for(ComponentNode component : components){
			String compName = component.getName();
			String prismCompName = prismRename(compName);
			String countString = Long.toString(component.getCount());
			sb.appendLine ("");
			sb.appendLine ("module " + prismCompName);
			sb.appendLine("");
			
			// A line like:
			// _E : [0..MAX] init 100;
			sb.append(prismCompName);
			sb.append(" : [0..MAX] init ");
			sb.append(countString);
			sb.append(";");
			sb.endLine();
			
			// Output lines such as:
			// [_r1] (_E >= 1) -> 1 : (_E’ = _E - 1);
			// [_rm1] (_E + 1 <= MAX) -> 1 : (_E’ = _E + 1);
			// [_r2] (_E + 1 <= MAX) -> 1 : (_E’ = _E + 1);
			for(SBAReaction reaction : reactions){
				List<SBAComponentBehaviour> products = reaction.getProducts();
				List<SBAComponentBehaviour> reactants = reaction.getReactants();
				
				
				// If any of the reactants are equal to the component
				// which we are defining then we output an equation
				// which decreases the component count by the amount
				// given in the stoichiometry.
				for(SBAComponentBehaviour reactant : reactants){
					if (compName.equals(reactant.getName())
							&& reactant.getType().equals(Type.REACTANT)){
						int stoichiom = reactant.getStoichiometry();
						String step = (levelSize == 1) ?
								      Integer.toString(stoichiom) :
								      ("(" + stoichiom + " * LEVELSIZE" + ")");
						sb.append("[");
						sb.append(prismRename(reaction.getName()));
						sb.append("] (");
						sb.append(prismCompName);
						sb.append(" >= ");
						sb.append(step);
						sb.append(") -> ");
						sb.append(" 1 ");
						sb.append(" : (");
						sb.append(prismCompName);
						sb.append("' = ");
						sb.append(prismCompName);
						sb.append(" - ");
						sb.append(step);
		                sb.append(");");
						sb.endLine();
					}
				}
				// Similarly if any of the products are equal to the
				// component we are describing then we output an
				// equation which increases its concentration
				for(SBAComponentBehaviour product : products){
					if (compName.equals(product.getName())){
						int stoichiom = product.getStoichiometry();
						String step = (levelSize == 1) ?
								      Integer.toString(stoichiom) :
								      ("(" + stoichiom + " * LEVELSIZE" + ")");
						sb.append("[");
						sb.append(prismRename(reaction.getName()));
						sb.append("] (");
						sb.append(prismCompName);
						sb.append(" + ");
						sb.append(step);
						sb.append(" <= MAX) -> ");
						sb.append(step);
						sb.append(" : (");
						sb.append(prismCompName);
						sb.append("' = ");
						sb.append(prismCompName);
						sb.append(" + ");
						sb.append(step);
		                sb.append(");");
						sb.endLine();
					}
				}
			}
			
			sb.appendLine("");
			sb.appendLine("endmodule");
			sb.appendLine("");
			
		}
		
		
		
		// Similarly for each species we wish to count the
		// population size so we want to output for each
		// component type something like:
		/*
		// rewards: "number of E molecules present"
		rewards "_E"
		  true : _E;
		endrewards
		// rewards: "square of number of E molecules present (used to calculate standard derivation
		rewards "_E_squared"
		  true : _E * _E;
		endrewards
        */
		int rewardsSoFar = 1 ;
		
		// First we do JUST the component names since this is
		// a common rewards count that we wish to do, that is
		// a simple time-series analysis.
		for (String compName : componentNames){
			String name = prismRename(compName);
			sb.append("// rewards: number of ");
		    sb.append(name);
		    sb.append(" molecules present");
		    sb.endLine();
		    sb.appendLine("// reward number " + rewardsSoFar);
		    sb.append("rewards \"");
		    sb.append(name);
		    sb.append("\"");
		    sb.endLine();
		    sb.append("    true : ");
		    sb.append(name);
		    sb.append(";");
		    sb.endLine();
		    sb.appendLine("endrewards");
		    rewardsSoFar++;
		}
		
		for (String compName : componentNames){
			String name = prismRename(compName);
			sb.append("// rewards: square of number of ");
			sb.append(name);
			sb.append(" molecules present ");
			sb.endLine();
			sb.appendLine ("// (used to calculate standard derivation)");
			sb.appendLine("// reward number " + rewardsSoFar);
			sb.append("rewards \"");
			sb.append(name);
			sb.append("_squared\"");
			sb.endLine();
			sb.append("    true : ");
			sb.append(name);
			sb.append(" * ");
			sb.append(name);
			sb.append(";");
			sb.endLine();
			sb.appendLine("endrewards");
			sb.appendLine("");
			rewardsSoFar++;
		}
		
		// Now for rewards, these are pretty simple:
		// First for reactions we wish to output something like:
		// rewards "_r1"
		//   [_r1] true : 1;
		// endrewards
        // for each reaction.
		sb.appendLine("// The rewards now for the reactions");
		sb.appendLine("// we count both actual prism action firings (abstracted)");
		sb.appendLine("// and the number of simulated reactions (simulated)");
		sb.appendLine("// The abstracted will of course depend on the level size");
		sb.appendLine("// but the simulated should be the same regardless of the");
		sb.appendLine("// level size, obviously depending on how much the abstraction");
		sb.appendLine("// affects the model");
		for(SBAReaction reaction : reactions){
			String basename = prismRename(reaction.getName());
			String absname = basename + "_abstracted";
			sb.appendLine("// count rewards for " + basename + " - abstracted");
			sb.appendLine("// reward number " + rewardsSoFar);
			sb.append("rewards \"");
			sb.append(absname);
			sb.append("\"");
			sb.endLine();
			
			sb.append("[");
			sb.append(basename);
			sb.append("] true : 1;");
			sb.endLine();
			sb.appendLine("endrewards");
			sb.appendLine("");
			rewardsSoFar++;
			
			String simname = basename + "_sim";
			sb.appendLine("// count rewards for " + basename + " - simulated");
			sb.appendLine("// reward number " + rewardsSoFar);
			sb.append("rewards \"");
			sb.append(simname);
			sb.append("\"");
			sb.endLine();
			
			sb.append("[");
			sb.append(basename);
			sb.append("] true : LEVELSIZE;");
			sb.endLine();
			sb.appendLine("endrewards");
			sb.appendLine("");
			rewardsSoFar++;
		}
		
		
		ps.appendLine ("// Constants:");
		ps.appendLine ("const double T; // time instant");
		ps.appendLine ("const int i; // number of molecules");
		ps.appendLine ("const int rew; // reward variable") ;
		
		ps.appendLine("// To use both of these select new experiment in xprism");
		ps.appendLine("// Range rew over the reward variables you wish to plot");
		ps.appendLine("// For a time series analysis use the top one");
		ps.appendLine("// For this model to plot all species range from 1 - " + 
				componentNames.length);
		ps.appendLine("");
		
		ps.appendLine("// Expected value of model component rew at time T (1 - " 
				+ componentNames.length + ")");
		ps.appendLine("R{rew}=? [ I=T ]");
		ps.appendLine("");
		ps.appendLine("// Expected long-run value of model component rew?");
		ps.appendLine("R{rew}=? [ S ]");

		
		
		for (String compName : componentNames){
			String name = prismRename(compName);
			ps.appendLine ("// Expected number of " + name 
					+ " molecules at time T?");
			ps.appendLine ("R{\"" + name + "\"}=? [ I=T ]");
			ps.appendLine ("// Expected long-run number of " + name + 
					" molecules?");
			ps.appendLine ("R{\"" + name + "\"}=? [ S ]");
			
			ps.appendLine ("// Probability of there being i molecules of species "
					+ name + " at time T?");
			ps.appendLine ("P=? [ true U[T,T] " + name + "=i ]");

			ps.appendLine ("// What is the probability of reaching the maximum "
					+ "number of molecules of species " + name + " at time T?");
			ps.appendLine("P=? [ true U[T,T] \"" + name + "_at_maximum\" ]");

			ps.appendLine("// What is the probability of having reached the "
					+ "maximum number of molecules of species " + name + " at time T or before?");
			ps.appendLine("P=? [ true U<=T \"" + name + "_at_maximum\" ]"); 

			ps.appendLine("// Stability of species " + name + " in the steady-state?");
			ps.appendLine("S=? [(" + name + " >= (i - 1)) & (" + name + " <= (i + 1)) ]");

			ps.appendLine("// Is species " + name + " depleted?");
			ps.appendLine("label \"" + name + "_depleted\" = " + name + " = 0;");

			ps.appendLine("// Is species " + name + " at its maximum value?");
			ps.appendLine("label \"" + name + "_at_maximum\" = " 
					+ name + " = MAX;");

		}
		return ;
	}

	private void separateString(String[] names, String sep, StringConsumer sb) throws IOException {
		int size = names.length;
		if (size == 0) {
			return;
		}
		for (int index = 0; index < size - 1; index++) {
			sb.append(names[index]);
			sb.append(sep);
		}
		sb.append(names[size - 1]);

	}

}
