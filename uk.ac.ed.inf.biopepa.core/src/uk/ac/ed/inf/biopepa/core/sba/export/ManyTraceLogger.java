package uk.ac.ed.inf.biopepa.core.sba.export;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.interfaces.Result;
import uk.ac.ed.inf.biopepa.core.sba.export.SimulationTracer.SimulationTraceLog;

/*
 * Allows us to combine many trace loggers into a single one
 * allowing us to generate more than one trace at a time.
 */
public class ManyTraceLogger implements SimulationTraceLog {
	
	private LinkedList<SimulationTraceLog> allMyLoggers;
	public ManyTraceLogger (){
		this.allMyLoggers = new LinkedList <SimulationTraceLog>();
	}
	
	public void addSimulationTraceLogger (SimulationTraceLog logger){
		this.allMyLoggers.add(logger);
	}

	public void displayComponentCounts(HashMap<String, Number> componentCounts) throws IOException {
		for (SimulationTraceLog logger : allMyLoggers){
			logger.displayComponentCounts(componentCounts);
		}
	}

	public void displayEnabledReaction(String rName, double rValue) throws IOException {
		for (SimulationTraceLog logger : allMyLoggers){
			logger.displayEnabledReaction(rName, rValue);
		}
	}

	public void endEvent(double thisDelay, double totalRate, 
			HashMap<String, Number> componentCounts)
			throws IOException {
		for (SimulationTraceLog logger : allMyLoggers){
			logger.endEvent(thisDelay, totalRate, componentCounts);
		}
	}

	public void outputComponentUpdate(String rName, int newValue) throws IOException {
		for (SimulationTraceLog logger : allMyLoggers){
			logger.outputComponentUpdate(rName, newValue);
		}
	}

	public void reportDeadlocked() throws IOException {
		for (SimulationTraceLog logger : allMyLoggers){
			logger.reportDeadlocked();
		}
	}

	public void startEvent(String rname, double totalTime) throws BioPEPAException, IOException {
		for (SimulationTraceLog logger : allMyLoggers){
			logger.startEvent(rname, totalTime);
		}
	}

	public void traceLogFooter(Result result) throws IOException {
		for (SimulationTraceLog logger : allMyLoggers){
			logger.traceLogFooter(result);
		}
	}

	public void traceLogHeader(HashMap<String, Number> componentCounts) throws IOException {
		for (SimulationTraceLog logger : allMyLoggers){
			logger.traceLogHeader(componentCounts);
		}
	}

}
