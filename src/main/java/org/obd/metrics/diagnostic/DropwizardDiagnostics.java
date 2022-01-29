package org.obd.metrics.diagnostic;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
		final SortedMap<String, Meter> meters = metrics.getMeters();
		if (meters.isEmpty()) {
			return Optional.empty();
		} else {
			for (final Entry<String, PidDefinition> pp : pidsMapping.entrySet()) {
				if (pp.getValue().getPriority() == 0) {
					final String key = pp.getKey();
					final Meter meter = meters.get(key);
					switch (rateType) {
					case MEAN:
						return getRate(key, meter.getMeanRate(), rateType);
					case ONE_MINUTE:
						return getRate(key, meter.getOneMinuteRate(), rateType);
					case FIVE_MINUTES:
						return getRate(key, meter.getFiveMinuteRate(), rateType);
					}
				}
			}
			final String key = meters.firstKey();
			return getRate(key, meters.get(key).getMeanRate(), rateType);
		}
	}

	@Override
	public Histogram findHistogramBy(PidDefinition pid) {
		return new DropwizardHistogram(findHistogram(pid).getSnapshot());
	}

	@Override
	public double getRateBy(RateType rateType, PidDefinition pid) {
		switch (rateType) {
		case MEAN:
			return findMeterBy(pid).getMeanRate();
		case ONE_MINUTE:
			return findMeterBy(pid).getOneMinuteRate();
		case FIVE_MINUTES:
			return findMeterBy(pid).getFiveMinuteRate();
		}
		return -1;
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
		if (rate == 0) {
			return Optional.empty();
		}
		return Optional.of(new Rate(rateType, rate, key));
	}
}
