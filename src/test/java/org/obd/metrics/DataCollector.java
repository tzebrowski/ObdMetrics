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
public final class DataCollector extends ReplyObserver {

	@Getter
	private MultiValuedMap<Command, Reply<?>> data = new ArrayListValuedHashMap<Command, Reply<?>>();

	@Override
	public void onNext(Reply<?> metric) {
		data.put(metric.command, metric);
		log.trace("Receive data: {}", metric.toString());
	}
}
