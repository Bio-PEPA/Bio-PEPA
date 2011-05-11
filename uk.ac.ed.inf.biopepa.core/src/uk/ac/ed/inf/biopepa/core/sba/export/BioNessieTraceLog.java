package uk.ac.ed.inf.biopepa.core.sba.export;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.sba.SBAModel;
import uk.ac.ed.inf.biopepa.core.sba.StringConsumer;
import uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer.SimulationTraceLog;


/*
 * This is a strange logger because actually bionessie does not
 * take in a trace, but time series, since all of the time points
 * are equally spaced. Hence this fakes a logger by doing nothing
 * during the actual simulation but then to close the trace file
 * it interprets the results into its own format.
 */
public class BioNessieTraceLog implements SimulationTraceLog {
	private StringConsumer scon;
	/*
	 * If this trace is a file (or other output) of its own then it should open
	 * and close the string consumer. However this trace may be part of a larger
	 * output and hence should not manage the open and closing of the file
	 * consumer.
	 */
	private boolean completeConsumer = true;

	/*
	 * If this trace is a file (or other output) of its own then it should open
	 * and close the string consumer. However this trace may be part of a larger
	 * output and hence should not manage the open and closing of the file
	 * consumer. Hence in the latter case the caller should call
	 * setCompleteConsumer(false) and manage the string consumer themselves.
	 */
	public void setCompleteConsumer(boolean complete) {
		this.completeConsumer = complete;
	}

	public BioNessieTraceLog(StringConsumer scon, SBAModel model) {
		this.scon = scon;
	}
	
	public void traceLogFooter(Result result) throws IOException {
		if(this.completeConsumer){
			this.scon.closeStringConsumer();
		}
		return ;
	}

	public void parameterSweepFormat(Result result) throws IOException {
		// if(this.completeConsumer){
		// 	this.scon.openStringConsumer();
		// }
		String [] cnames = result.getComponentNames();
		
		// First make the header line, the first value is
		// the time value called T
		scon.append("T");
		for (String name : cnames){
			scon.append("|");
			scon.append(name);
		}
		scon.endLine();
		
		// Now for each time value we output the value of each
		// component at that time
		double [] timepoints = result.getTimePoints();
		/*double [][] timeSeries = new double[cnames.length][];
		for (int nameIndex = 0; nameIndex < cnames.length; nameIndex++){
			timeSeries[nameIndex] = result.getTimeSeries(nameIndex);
		}*/
		for (int timeIndex = 0; timeIndex < timepoints.length; timeIndex++){
			double time = timepoints[timeIndex];
			scon.append(Double.toString(time));
			for (int nameIndex = 0; nameIndex < cnames.length; nameIndex++){
				double value = result.getTimeSeries(nameIndex)[timeIndex];
				scon.append("|" + value);
			}
			scon.endLine();
		}
		
		// if(this.completeConsumer){
		// 	this.scon.closeStringConsumer();
		// }
		return ;
	}

	private String[] cnames;
	public void traceLogHeader(HashMap<String, Number> componentCounts) throws IOException {
		/* Potentially we could open the file consumer
		 * at this point, but since we do not actually output anything
		 * until we get to the footer we instead open the file there
		 * (if of course this is a complete consumer).
		 */
		if(this.completeConsumer){
			this.scon.openStringConsumer();
		}
		
		Set<String> cnamesSet = componentCounts.keySet();
		cnames = new String [cnamesSet.size()];
		int nameIndex = 0;
		for (String cname : cnamesSet){
			cnames[nameIndex] = cname;
			nameIndex++;
		}
		
		
		// First make the header line, the first value is
		// the time value called T
		scon.append("Time");
		for (String name : cnames){
			scon.append(" ");
			scon.append(name);
		}
		scon.endLine();
		
		scon.append("0.0");
		endEvent(0.0, 0.0, componentCounts);
		
		return;
	}

	public void displayComponentCounts(HashMap<String, Number> componentCounts) throws IOException {
		return ;
	}

	public void displayEnabledReaction(String reactionName, double rateValue) throws IOException {
		return ;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer.SimulationTraceLog
	 * #startEvent(java.lang.String, double) The arguments to startEvent and
	 * stopEvent are set up for traviando traces, we should either pass all
	 * information into each of them or find a better way to log events.
	 */
	public void startEvent(String rname, double totalTime) throws BioPEPAException, IOException {
		scon.append(Double.toString(totalTime));
		return ;
	}

	public void outputComponentUpdate(String rName, int newValue) throws IOException {
		return;
	}
	
	public void endEvent(double thisDelay, double totalRate, 
			HashMap<String, Number> componentCounts)
			throws IOException {
		// Note that 'startEvent' has already added the current time, so
		// the space at the start of the first value is correct.
		// Also traceLogHeader also calls this to complete the 0.0 line so
		// if we change this to add something more then we need to look there
		// as well.
		for (int nameIndex = 0; nameIndex < cnames.length; nameIndex++){
			double value = componentCounts.get(cnames[nameIndex]).doubleValue(); 
			scon.append(" " + value);
		}
		scon.endLine();
		return ;
	}

	public void reportDeadlocked() throws IOException {
		return ;
	}

}
