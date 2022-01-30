package org.obd.metrics.diagnostic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;

import org.obd.metrics.ObdMetric;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.pid.PidDefinition;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DropwizardDiagnostics extends ReplyObserver<ObdMetric> implements Diagnostics {

	private MetricRegistry metrics = new MetricRegistry();
	private final Map<String, PidDefinition> pidsMapping = new HashMap<String, PidDefinition>();

	@Override
	public void onNext(ObdMetric obdMetric) {
		final PidDefinition pidDefinition = obdMetric.getCommand().getPid();
		log.trace("Update histogram: {} {}", pidDefinition.getPid(), obdMetric.valueToLong());
		final com.codahale.metrics.Histogram histogram = findHistogram(pidDefinition);
		histogram.update(obdMetric.valueToLong());
		findMeterBy(pidDefinition).mark();
		pidsMapping.put(getMeterKey(pidDefinition), pidDefinition);
	}

	@Override
	public void reset() {
		metrics = new MetricRegistry();
		pidsMapping.clear();
	}

	@Override
	public Optional<Rate> getRateBy(RateType rateType) {
		if (metrics.getMeters().isEmpty()) {
			return Optional.empty();
		} else {
			return pidsMapping.entrySet()
			        .stream()
			        .filter(p -> p.getValue().getPriority() == 0)
			        .map(p -> getRate(rateType, p.getKey(), metrics.getMeters().get(p.getKey())))
			        .findFirst()
			        .orElse(firstKeyRate(rateType));
		}
	}

	@Override
	public Histogram findHistogramBy(PidDefinition pid) {
		return new DropwizardHistogram(findHistogram(pid).getSnapshot());
	}

	@Override
	public Optional<Rate> getRateBy(RateType rateType, PidDefinition pid) {
		final String key = getMeterKey(pid);
		final Meter meter = findMeterBy(pid);
		return getRate(rateType, key, meter);
	}

	private Meter findMeterBy(PidDefinition pid) {
		return metrics.meter(getMeterKey(pid));
	}

	private String getMeterKey(PidDefinition pid) {
		return "meter." + pid.getId();
	}

	private com.codahale.metrics.Histogram findHistogram(PidDefinition pid) {
		return metrics.histogram("hist." + pid.getId());
	}

	private Optional<Rate> getRate(final String key, final double rate, final RateType rateType) {
		if (log.isTraceEnabled()) {
			log.trace("Key: {}, rate: {}", key, rate);
		}

		return Optional.of(new Rate(rateType, rate, key));
	}

	private Optional<Rate> getRate(RateType rateType, final String key, final Meter meter) {
		switch (rateType) {
		default:
			return getRate(key, meter.getMeanRate(), rateType);
		case ONE_MINUTE:
			return getRate(key, meter.getOneMinuteRate(), rateType);
		case FIVE_MINUTES:
			return getRate(key, meter.getFiveMinuteRate(), rateType);
		case MEAN:
			return getRate(key, meter.getMeanRate(), rateType);
		}
	}

	private Optional<Rate> firstKeyRate(RateType rateType) {
		final SortedMap<String, Meter> meters = metrics.getMeters();
		final String key = meters.firstKey();
		return getRate(key, meters.get(key).getMeanRate(), rateType);
	}
}
