package org.obd.metrics.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;

import org.obd.metrics.ObdMetric;
import org.obd.metrics.ReplyObserver;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.obd.SupportedPidsCommand;
import org.obd.metrics.pid.PidDefinition;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DropwizardStatisticsRegistry extends ReplyObserver<ObdMetric> implements StatisticsRegistry {

	private MetricRegistry metrics = new MetricRegistry();
	private final Map<String, PidDefinition> meterPids = new HashMap<String, PidDefinition>();

	@Override
	public void onNext(ObdMetric obdMetric) {
		final ObdCommand command = obdMetric.getCommand();
		if (!(command instanceof SupportedPidsCommand)) {
			final Histogram histogram = findHistogramBy(obdMetric.getCommand().getPid());
			histogram.update(obdMetric.valueToLong());
			findMeterBy(obdMetric.getCommand().getPid()).mark();

			meterPids.put(getMeterKey(obdMetric.getCommand().getPid()), obdMetric.getCommand().getPid());
		}
	}

	@Override
	public void reset() {
		metrics = new MetricRegistry();
		meterPids.clear();
	}

	@Override
	public Optional<RatePerSec> getRatePerSec() {
		final SortedMap<String, Meter> meters = metrics.getMeters();
		if (meters.isEmpty()) {
			return Optional.empty();
		} else {
			for (final Entry<String, PidDefinition> pp : meterPids.entrySet()) {
				if (pp.getValue().getPriority() == 0) {
					String firstKey = pp.getKey();
					final double meanRate = meters.get(firstKey).getMeanRate();
					return toRateSpec(firstKey, meanRate);
				}
			}

			final String key = meters.firstKey();
			final double meanRate = meters.get(key).getMeanRate();

			return toRateSpec(key, meanRate);
		}
	}

	@Override
	public MetricStatistics findBy(PidDefinition pid) {
		return new DropwizardMetricsStatistics(findHistogramBy(pid).getSnapshot());
	}

	@Override
	public double getRatePerSec(PidDefinition pid) {
		return findMeterBy(pid).getMeanRate();
	}

	private Meter findMeterBy(PidDefinition pid) {
		return metrics.meter(getMeterKey(pid));
	}

	private String getMeterKey(PidDefinition pid) {
		return "meter." + pid.getId();
	}

	private Histogram findHistogramBy(PidDefinition pid) {
		return metrics.histogram("hist." + pid.getId());
	}

	private Optional<RatePerSec> toRateSpec(String firstKey, final double meanRate) {
		if (log.isTraceEnabled()) {
			log.trace("Key: {}, rate: {}", firstKey, meanRate);
		}
		if (meanRate == 0) {
			return Optional.empty();
		}
		return Optional.of(new RatePerSec(meanRate, firstKey));
	}
}
