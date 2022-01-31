package org.obd.metrics.diagnostic;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class DefaultHistogram implements Histogram {
	private final com.dynatrace.dynahist.Histogram delegate;

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
		return delegate.getQuantile(0.5);
	}
}