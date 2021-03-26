package org.obd.metrics;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class DataCollector extends ReplyObserver<Reply<?>> {

	@Getter
	private MultiValuedMap<Command, Reply<?>> data = new ArrayListValuedHashMap<Command, Reply<?>>();

	@Getter
	private MultiValuedMap<PidDefinition, ObdMetric> metrics = new ArrayListValuedHashMap<PidDefinition, ObdMetric>();

	@Override
	public void onNext(Reply<?> reply) {
		log.info("Receive data: {}", reply.toString());
		data.put(reply.getCommand(), reply);

		if (reply instanceof ObdMetric) {
			metrics.put(((ObdMetric) reply).getCommand().getPid(), (ObdMetric) reply);
		}
	}
}
