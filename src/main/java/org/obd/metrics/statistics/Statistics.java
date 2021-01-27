package org.obd.metrics.statistics;

public interface Statistics {
	int size();

	double getMedian();

	long getMax();

	double getMean();

	long getMin();
}