package org.obd.metrics.diagnostic;

import com.codahale.metrics.Snapshot;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class DropwizardHistogram implements Histogram {
	private final Snapshot delegate;

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public double getMax() {
		return delegate.getMax();
	}

	@Override
	public double getMin() {
		return delegate.getMin();
	}

	@Override
	public double getMean() {
		return delegate.getMean();
	}
}