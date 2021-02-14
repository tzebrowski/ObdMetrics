package org.obd.metrics.statistics;

public interface Statistics {
	int size();

	double getMedian();

	long getMax();

	long getMin();
}