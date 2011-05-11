/*******************************************************************************
 * Copyright (c) 2009 University of Edinburgh.
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the BSD Licence, which accompanies this feature
 * and can be downloaded from http://groups.inf.ed.ac.uk/pepa/update/licence.txt
 ******************************************************************************/
package uk.ac.ed.inf.biopepa.core.interfaces;

public interface ProgressMonitor {

	public static final int UNKNOWN = -1;

	public void beginTask(int amount);

	public void setCanceled(boolean state);

	public boolean isCanceled();

	/*
	 * Indicates that the given number of units of work
	 * have been completed. Note that this represents an
	 * installment of the work rather than a cumulative amount.
	 * So for example it should be used as in:
	 * void runSimulations (ProgressMonitor monitor, int replications){
	 *    int done = 0;
	 *    monitor.beginTask(replications);
	 *    while (done < replications){
	 *      performSimulation();
	 *      monitor.worked(1); // NOTE: 1 and not 'done'
	 *    }
	 * }
	 * Of course this is only accurate if each simulation takes about
	 * the same time, you can do better with subtasks, but this interface
	 * does not implement them.
	 * 
	 */
	public void worked(int worked);

	public void done();

}
