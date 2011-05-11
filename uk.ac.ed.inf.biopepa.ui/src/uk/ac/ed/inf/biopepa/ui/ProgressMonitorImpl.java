package uk.ac.ed.inf.biopepa.ui;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.ed.inf.biopepa.core.interfaces.ProgressMonitor;

public class ProgressMonitorImpl implements ProgressMonitor {
	IProgressMonitor eclipseMonitor;

	String name;

	public ProgressMonitorImpl(String name, IProgressMonitor monitor) {
		eclipseMonitor = monitor;
		this.name = name;
	}

	public void beginTask(int amount) {
		eclipseMonitor
				.beginTask(
						name,
						(amount == ProgressMonitor.UNKNOWN ? IProgressMonitor.UNKNOWN
								: amount));
	}

	public void done() {
		eclipseMonitor.done();
	}

	public boolean isCanceled() {
		return eclipseMonitor.isCanceled();
	}

	public void setCanceled(boolean state) {
	}

	public void worked(int worked) {
		eclipseMonitor.worked(worked);
	}
}
