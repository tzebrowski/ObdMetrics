package org.obd.metrics.diagnostic;

public interface Histogram {

	double getMax();

	double getMin();

	Double getMean();
	
	Double getLatestValue();
	
}