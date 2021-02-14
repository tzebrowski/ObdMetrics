package org.obd.metrics.statistics;

import com.codahale.metrics.Snapshot;

final class DefaultStatistics implements Statistics {
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
	public long getMin() {
		return delegate.getMin();
	}
}