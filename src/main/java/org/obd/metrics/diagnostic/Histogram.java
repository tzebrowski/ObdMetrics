package org.obd.metrics.diagnostic;

public interface Histogram {
	int size();
	
	double getMax();

	double getMin();
	
	double getMean();
}