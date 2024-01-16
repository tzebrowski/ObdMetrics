/** 
 * Copyright 2019-2024, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.diagnostic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;

import org.obd.metrics.api.model.ObdMetric;
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
	private final Map<String, PidDefinition> mapping = new HashMap<>();

	void update(final ObdMetric obdMetric) {
		final PidDefinition pidDefinition = obdMetric.getCommand().getPid();
		log.trace("Update PID command rate: {}", pidDefinition.getPid());
		findMeterBy(pidDefinition).mark();
		mapping.put(getMeterKey(pidDefinition), pidDefinition);
	}

	@Override
	public void reset() {
		metrics = new MetricRegistry();
		mapping.clear();
	}

	@Override
	public Optional<Rate> findBy(final RateType rateType) {
		if (metrics.getMeters().isEmpty()) {
			return Optional.empty();
		} else {
			return mapping.entrySet().stream().filter(p -> p.getValue().getPriority() == 0)
					.map(p -> getRate(rateType, p.getKey(), metrics.getMeters().get(p.getKey()))).findFirst()
					.orElse(firstKeyRate(rateType));
		}
	}

	@Override
	public Optional<Rate> findBy(final RateType rateType, final PidDefinition pid) {
		final String key = getMeterKey(pid);
		return getRate(rateType, key, findMeterBy(pid));
	}

	private Meter findMeterBy(final PidDefinition pid) {
		return metrics.meter(getMeterKey(pid));
	}

	private String getMeterKey(final PidDefinition pid) {
		if (!meterKeyMap.containsKey(pid)) {
			meterKeyMap.put(pid, METER_KEY + pid.getId());
		}
		return meterKeyMap.get(pid);
	}

	private Optional<Rate> getRate(final String key, final double rate, final RateType rateType) {
		log.trace("Key: {}, rate: {}", key, rate);
		return Optional.of(new Rate(rateType, rate, key));
	}

	private Optional<Rate> getRate(final RateType rateType, final String key, final Meter meter) {
		switch (rateType) {
		default:
		case MEAN:
			return getRate(key, meter.getMeanRate(), rateType);
		}
	}

	private Optional<Rate> firstKeyRate(final RateType rateType) {
		final SortedMap<String, Meter> meters = metrics.getMeters();
		final String key = meters.firstKey();
		return getRate(key, meters.get(key).getMeanRate(), rateType);
	}
}
