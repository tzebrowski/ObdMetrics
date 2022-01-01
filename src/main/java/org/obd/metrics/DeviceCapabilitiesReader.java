package org.obd.metrics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	public String[] observables() {
		return new String[] {
		        SupportedPidsCommand.class.getName()
		};
	}
}
