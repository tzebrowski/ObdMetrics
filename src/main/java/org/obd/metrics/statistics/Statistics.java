package org.obd.metrics.statistics;

public interface Statistics {
	int size();

	long getMedian();

	long getMax();

	long getMin();
}