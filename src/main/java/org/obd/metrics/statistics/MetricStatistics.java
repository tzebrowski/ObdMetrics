package org.obd.metrics.statistics;

public interface MetricStatistics {
	int size();

	double getMedian();

	public abstract long getMax();

	public abstract double getMean();

	public abstract long getMin();
}