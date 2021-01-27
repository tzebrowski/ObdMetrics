package org.obd.metrics.statistics;

import com.codahale.metrics.Snapshot;

final class DefaultStatistics implements MetricStatistics {
	private final Snapshot delegate;

	DefaultStatistics(Snapshot snap) {
		this.delegate = snap;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public double getMedian() {
		return delegate.getMedian();
	}

	@Override
	public long getMax() {
		return delegate.getMax();
	}

	@Override
	public double getMean() {
		return delegate.getMean();
	}

	@Override
	public long getMin() {
		return delegate.getMin();
	}
}