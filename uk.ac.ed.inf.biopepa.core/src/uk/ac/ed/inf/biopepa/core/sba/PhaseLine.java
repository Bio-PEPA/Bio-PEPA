package uk.ac.ed.inf.biopepa.core.sba;

public class PhaseLine {

	private double duration;
	private ExperimentLine experimentLine;
	
	public PhaseLine(ExperimentLine el, double duration) {
		this.experimentLine = el;
		this.duration = duration;
	}
	
	public double getDuration(){
		return this.duration;
	}
	
	public ExperimentLine getExperimentLine(){
		return this.experimentLine;
	}

	
	
}
