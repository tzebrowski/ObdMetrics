package org.obd.metrics;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.command.Command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class DataCollector extends MetricsObserver {

	@Getter
	private MultiValuedMap<Command, Metric<?>> data = new ArrayListValuedHashMap<Command, Metric<?>>();

	@Override
	public void onNext(Metric<?> metric) {
		log.info("Receive data: {}", metric);
	}
}
