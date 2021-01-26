package org.obd.metrics.statistics;

import com.codahale.metrics.Snapshot;

final class DefaultStatistics implements MetricStatistics {
	private final Snapshot snapshot;

	DefaultStatistics(Snapshot snap) {
		this.snapshot = snap;
	}

	@Override
	public int size() {
		return snapshot.size();
	}

	@Override
	public double getMedian() {
		return snapshot.getMedian();
	}

	@Override
	public long getMax() {
		return snapshot.getMax();
	}

	@Override
	public double getMean() {
		return snapshot.getMean();
	}

	@Override
	public long getMin() {
		return snapshot.getMin();
	}
}