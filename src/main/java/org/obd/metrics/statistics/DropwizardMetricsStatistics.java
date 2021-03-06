package org.obd.metrics.statistics;

import com.codahale.metrics.Snapshot;

final class DropwizardMetricsStatistics implements MetricStatistics {
	private final Snapshot delegate;

	DropwizardMetricsStatistics(Snapshot snap) {
		this.delegate = snap;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public long getMedian() {
		return (long) delegate.getMedian();
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