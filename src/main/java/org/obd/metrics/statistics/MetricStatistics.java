package org.obd.metrics.statistics;

public interface MetricStatistics {
	int size();

	long getMedian();

	long getMax();

	long getMin();
}