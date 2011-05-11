/*******************************************************************************
 * Copyright (c) 2006, 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD Licence, which
 * accompanies this feature and can be downloaded from
 * http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 *******************************************************************************/
package uk.ac.ed.inf.common.launching;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.RuntimeProcess;

public class RuntimeMonitorWithLock extends RuntimeProcess {

	private Object fLock = new Object();

	@SuppressWarnings("unchecked")
	public RuntimeMonitorWithLock(ILaunch launch, Process process, String name,
			Map attributes) {
		super(launch, process, name, attributes);
	}

	protected void fireTerminateEvent() {
		super.fireTerminateEvent();
		synchronized (fLock) {
			fLock.notifyAll();

		}
	}

	public synchronized int waitFor() throws DebugException {
		while (!isTerminated())
			try {
				synchronized (fLock) {
					fLock.wait();
				}
			} catch (InterruptedException e) {
			}
		Assert.isTrue(isTerminated());
		return this.getExitValue();
	}
}