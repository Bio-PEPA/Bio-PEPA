package uk.ac.ed.inf.biopepa.core.sba;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.Model;
import uk.ac.ed.inf.biopepa.core.dom.OverrideValueVisitor;
import uk.ac.ed.inf.biopepa.core.interfaces.Result;

public class ExperimentLine {
	private String experimentLineName;
	private Result experimentLineResult;

	public ExperimentLine(String name) {
		this.experimentLineName = name;
		this.experimentLineResult = null;
		this.initialComponentPops = new HashMap<String, Number>();
		this.rateValues = new HashMap<String, Number>();
		this.reactionActivations = new HashMap<String, Boolean>();
		this.specialDefines = new HashMap<String, Number>();
	}

	public String getName() {
		return experimentLineName;
	}

	public void setName(String name) {
		this.experimentLineName = name;
	}

	public void setResult(Result r) {
		this.experimentLineResult = r;
	}

	public boolean hasResult() {
		return this.experimentLineResult != null;
	}

	public Result getResult() {
		return this.experimentLineResult;
	}

	public Map<String, Number> initialComponentPops;
	public Map<String, Number> rateValues;
	public Map<String, Boolean> reactionActivations;
	// such as phase-delay, so that any users of
	// experiment lines/sets for input can define special
	// names outwith those used in the model.
	public Map<String, Number> specialDefines;

	public void addInitialConcentration(String compName, Number value) {
		initialComponentPops.put(compName, value);
	}

	public Map<String, Number> getInitialPopulations() {
		return initialComponentPops;
	}

	public Number getInitialPopulation(String compName) {
		return initialComponentPops.get(compName);
	}

	public Map<String, Number> getRateValues(){
		return rateValues;
	}
	
	public void addRateValue(String rateName, Number value) {
		rateValues.put(rateName, value);
	}

	public Number getRateValue(String rateName) {
		return rateValues.get(rateName);
	}

	public Map<String, Boolean> getReactionActivations(){
		return reactionActivations;
	}
	
	public Set<String> getRateNames() {
		return rateValues.keySet();
	}

	public boolean isReactionActiviated(String reactionName) {
		Boolean answer = reactionActivations.get(reactionName);
		// The default is for the reaction to be activated
		if (answer == null) {
			return true;
		}
		return answer.booleanValue();
	}

	public void addReactionActivation(String reactionName, boolean activated) {
		reactionActivations.put(reactionName, activated);
	}

	public Set<String> getReactionNames() {
		return reactionActivations.keySet();
	}
	
	public void addSpecialDefine (String name, Number value){
		specialDefines.put(name, value);
	}
	public Number getSpecialDefine (String name){
		return specialDefines.get(name);
	}
	
	public void applyToAst (Model astModel) throws BioPEPAException {
		for (Entry<String, Number> override : initialComponentPops.entrySet()){
			String name = override.getKey();
			String value = Integer.toString(override.getValue().intValue());
			//String value = override.getValue().toString();
			OverrideValueVisitor ovvisitor = new OverrideValueVisitor (name, value);
			astModel.accept(ovvisitor);
			/* TODO: We should probably do something in this case
			if (ovvisitor.getValueOverridden() == false){
			}
			*/
		}
		for (Entry<String, Number> override : rateValues.entrySet()){
			String name = override.getKey();
			String value = override.getValue().toString();
			OverrideValueVisitor ovvisitor = new OverrideValueVisitor (name, value);
			astModel.accept(ovvisitor);
			/* TODO: We should probably do something in this case
			if (ovvisitor.getValueOverridden() == false){
			}
			*/
		}
		for (Entry<String, Boolean> override : reactionActivations.entrySet()){
			if (override.getValue().booleanValue() == false){
				String name = override.getKey();
				String value = "off";
				OverrideValueVisitor ovvisitor = new OverrideValueVisitor (name, value);
				astModel.accept(ovvisitor);
				/* TODO: We should probably do something in this case
				if (ovvisitor.getValueOverridden() == false){
				}
				 */
			}
		}
	}
}
