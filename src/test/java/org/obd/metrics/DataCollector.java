package org.obd.metrics;

import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.api.ObdMetric;
import org.obd.metrics.api.Reply;
import org.obd.metrics.api.ReplyObserver;
import org.obd.metrics.command.ATCommand;
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
	private final MultiValuedMap<Command, Reply<?>> data = new ArrayListValuedHashMap<Command, Reply<?>>();

	private final MultiValuedMap<PidDefinition, ObdMetric> metrics = new ArrayListValuedHashMap<PidDefinition, ObdMetric>();

	public ObdMetric findSingleMetricBy(PidDefinition pidDefinition) {
		List<ObdMetric> list = (List<ObdMetric>) metrics.get(pidDefinition);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	public List<ObdMetric> findMetricsBy(PidDefinition pidDefinition) {
		return (List<ObdMetric>) metrics.get(pidDefinition);
	}

	public Reply<?> findATResetCommand() {
		final ATCommand key = new ATCommand("Z");
		if (data.containsKey(key)) {
			final List<Reply<?>> collection = (List<Reply<?>>) data.get(key);
			if (!collection.isEmpty()) {
				return collection.get(0);
			}
		} 
		return null;
	}

	@Override
	public void onNext(Reply<?> reply) {
		log.trace("Receive data: {}", reply.toString());
		data.put(reply.getCommand(), reply);

		if (reply instanceof ObdMetric) {
			metrics.put(((ObdMetric) reply).getCommand().getPid(), (ObdMetric) reply);
		}
	}
}
