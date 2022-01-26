package org.obd.metrics.statistics;

public interface MetricStatistics {
	int size();
	
	long getMax();

	long getMin();
	
    double getMean();

}