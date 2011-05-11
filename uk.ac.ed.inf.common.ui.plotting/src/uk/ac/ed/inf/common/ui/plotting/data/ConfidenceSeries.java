package uk.ac.ed.inf.common.ui.plotting.data;

/**
 * A series of data with confidence intervals
 * 
 * @author Mirco
 * 
 */
public class ConfidenceSeries extends Series {

	private double[] radii;

	private double confidenceLevel;

	public static ConfidenceSeries create(double[] averages, double[] radii,
			String label, double confidenceLevel) {
		if (averages == null || radii == null)
			throw new NullPointerException();
		if (averages.length != radii.length)
			throw new IllegalArgumentException();
		return new ConfidenceSeries(averages, radii, label, confidenceLevel);

	}

	ConfidenceSeries(double[] averages, double[] radii, String label,
			double confidenceLevel) {
		super(averages, label);
		this.confidenceLevel = confidenceLevel;
		this.radii = new double[radii.length];
		System.arraycopy(radii, 0, this.radii, 0, radii.length);
	}

	public double[] getRadii() {
		return radii;
	}
	
	public double getConfidenceLevel() {
		return confidenceLevel;
	}

}
