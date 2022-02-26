package org.obd.metrics.diagnostic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;

import org.obd.metrics.ObdMetric;
import org.obd.metrics.pid.PidDefinition;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DefaultRateCollector implements RateSupplier {

	private final Map<PidDefinition, String> meterKeyMap = new HashMap<>();
	
	private static final String METER_KEY = "meter.";
	private MetricRegistry metrics = new MetricRegistry();
	private final Map<String, PidDefinition> mapping = new HashMap<String, PidDefinition>();

	void update(ObdMetric obdMetric) {
		final PidDefinition pidDefinition = obdMetric.getCommand().getPid();
		log.trace("Update PID command rate: {}", pidDefinition.getPid());
		findMeterBy(pidDefinition).mark();
		mapping.put(getMeterKey(pidDefinition), pidDefinition);
	}

	void reset() {
		metrics = new MetricRegistry();
		mapping.clear();
	}

	@Override
	public Optional<Rate> findBy(RateType rateType) {
		if (metrics.getMeters().isEmpty()) {
			return Optional.empty();
		} else {
			return mapping.entrySet()
			        .stream()
			        .filter(p -> p.getValue().getPriority() == 0)
			        .map(p -> getRate(rateType, p.getKey(), metrics.getMeters().get(p.getKey())))
			        .findFirst()
			        .orElse(firstKeyRate(rateType));
		}
	}

	@Override
	public Optional<Rate> findBy(RateType rateType, PidDefinition pid) {
		final String key = getMeterKey(pid);
		return getRate(rateType, key, findMeterBy(pid));
	}

	private Meter findMeterBy(PidDefinition pid) {
		return metrics.meter(getMeterKey(pid));
	}

	private String getMeterKey(PidDefinition pid) {
		if (!meterKeyMap.containsKey(pid)) {
			meterKeyMap.put(pid, METER_KEY + pid.getId());
		}
		return meterKeyMap.get(pid);
	}

	private Optional<Rate> getRate(String key, double rate, RateType rateType) {
		log.trace("Key: {}, rate: {}", key, rate);
		return Optional.of(new Rate(rateType, rate, key));
	}

	private Optional<Rate> getRate(RateType rateType, String key, Meter meter) {
		switch (rateType) {
		default:
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
