package org.obd.metrics.diagnostic;

import java.util.Optional;

import org.obd.metrics.ObdMetric;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultDiagnostics extends ReplyObserver<ObdMetric> implements Diagnostics {

	private final RateCollector rateCollector = new RateCollector();
	private final HistogramBuilder histogramCollector = new HistogramBuilder();
	
	@Override
	public void onNext(ObdMetric obdMetric) {
		rateCollector.update(obdMetric);
		histogramCollector.update(obdMetric);
	}

	@Override
	public void reset() {
		rateCollector.reset();
		histogramCollector.reset();
	}

	@Override
	public Optional<Rate> getRateBy(RateType rateType) {
		return rateCollector.getRateBy(rateType);
	}

	@Override
	public Histogram findHistogramBy(PidDefinition pid) {
		return histogramCollector.findHistogramBy(pid);
	}

	@Override
	public Optional<Rate> getRateBy(RateType rateType, PidDefinition pid) {
		return rateCollector.getRateBy(rateType, pid);
	}
}
