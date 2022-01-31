package org.obd.metrics.diagnostic;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class DefaultHistogram implements Histogram {
	private final com.dynatrace.dynahist.Histogram delegate;

	@Override
	public double getMax() {
		return normalize(delegate.getMax());
	}

	@Override
	public double getMin() {
		return normalize(delegate.getMin());
	}

	@Override
	public double getMean() {
		return normalize(delegate.getQuantile(0.5));
	}

	public double normalize(double value) {
		return value == Double.NaN || value == Double.NEGATIVE_INFINITY || value == Double.POSITIVE_INFINITY ? 0.0
		        : value;
	}
}