package org.obd.metrics.executor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.command.obd.SupportedPidsCommand;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DeviceCapabilitiesReader extends ReplyObserver<Reply<?>> {

	@Getter
	private Set<String> capabilities = new HashSet<>();

	@SuppressWarnings("unchecked")
	@Override
	public void onNext(Reply<?> reply) {
		final ObdMetric obdMetric = (ObdMetric) reply;
		if (obdMetric.getCommand() instanceof SupportedPidsCommand) {
			log.debug("Received device capabilities: {}", obdMetric.getValue());
			capabilities.addAll((List<String>) obdMetric.getValue());
		}
	}

	@Override
	public List<Class<?>> subscribeFor() {
		return Arrays.asList(SupportedPidsCommand.class);
	}
}
