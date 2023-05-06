package org.obd.metrics.command;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class GearEngaged extends Command implements Codec<String> {

	@Getter
	private final PidDefinition pid;

	public GearEngaged(PidDefinition pid) {
		super(pid.getQuery(), pid.getMode(), pid.getDescription());
		this.pid = pid;
	}

	@Override
	public String decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {

		final String message = connectorResponse.getMessage();
		if (log.isDebugEnabled()) {
			log.debug("Processing message: {}", pid.getPid(), message);
		}

		if (connectorResponse.isResponseCodeSuccess(pid)) {
			if (message.endsWith("DD")) {
				return "P";
			} else if (message.endsWith("EE")) {
				return "R";
			} else if (message.endsWith("11")) {
				return "1";
			} else if (message.endsWith("11")) {
				return "1";
			} else if (message.endsWith("22")) {
				return "2";
			} else if (message.endsWith("33")) {
				return "3";
			} else if (message.endsWith("44")) {
				return "4";
			} else if (message.endsWith("55")) {
				return "5";
			} else if (message.endsWith("66")) {
				return "6";
			} else if (message.endsWith("77")) {
				return "7";
			} else if (message.endsWith("88")) {
				return "8";
			} else {
				return "U";
			}
			
		} else {
			log.warn("Failed to transform message: {}", pid.getPid(), message);
			return null;
		}
	}
}
