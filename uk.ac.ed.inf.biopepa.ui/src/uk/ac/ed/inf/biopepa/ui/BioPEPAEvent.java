/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.ui;

import uk.ac.ed.inf.biopepa.ui.interfaces.BioPEPAModel;

public class BioPEPAEvent {
	
	public enum Event {
		PARSED, SOLVED, MODIFIED, EXCEPTION;
	}
	
	Event event;
	long timeElapsed = 0;
	BioPEPAModel model;
	Exception exception;
	
	public BioPEPAEvent(BioPEPAModel model, Event event, long duration) {
		this.model = model;
		if(event.equals(Event.EXCEPTION))
			throw new IllegalArgumentException("");
		this.event = event;
		this.timeElapsed = duration;
	}
	
	public BioPEPAEvent(BioPEPAModel model, Exception exception) {
		this.model = model;
		event = Event.EXCEPTION;
		this.exception = exception;
	}
	
	public Event getEvent() {
		return event;
	}
	
	public long getDuration() {
		return timeElapsed;
	}
	
	public BioPEPAModel getModel() {
		return model;
	}
	
	public Exception getThrownException() {
		return exception;
	}

}
