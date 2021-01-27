package org.obd.metrics.statistics;

public interface MetricStatistics {
	int size();

	double getMedian();

	long getMax();

	double getMean();

	long getMin();
}