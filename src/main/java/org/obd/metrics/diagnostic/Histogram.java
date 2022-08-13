package org.obd.metrics.diagnostic;

public interface Histogram {

	double getMax();

	double getMin();

	double getMean();
	
	double getLatestValue();
	
}