package uk.ac.ed.inf.biopepa.cl;

public class ReactionTracer {
	private String reactionName;
	private String tracerName;
	ReactionTracer (String reactionName, String tracerName){
		this.reactionName = reactionName;
		this.tracerName = tracerName;
	}
	
	public String getReactionName(){
		return this.reactionName;
	}
	public String getTracerName (){
		return this.tracerName;
	}
}
