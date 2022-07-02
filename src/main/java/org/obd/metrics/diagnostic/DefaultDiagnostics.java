package org.obd.metrics.diagnostic;

import org.obd.metrics.api.ObdMetric;
import org.obd.metrics.api.ReplyObserver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultDiagnostics extends ReplyObserver<ObdMetric> implements Diagnostics {

	private final DefaultRateCollector rate = new DefaultRateCollector();
	private final DefaultHistogramBuilder histogram = new DefaultHistogramBuilder();

	@Override
	public void onNext(final ObdMetric obdMetric) {
		rate.update(obdMetric);
		histogram.update(obdMetric);
	}

	@Override
	public void reset() {
		rate.reset();
		histogram.reset();
	}

	@Override
	public RateSupplier rate() {
		return rate;
	}

	@Override
	public HistogramSupplier histogram() {
		return histogram;
	}
}
