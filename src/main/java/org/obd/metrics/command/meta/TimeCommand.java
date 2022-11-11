package org.obd.metrics.command.meta;

import java.util.Optional;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TimeCommand extends MetadataCommand implements Codec<Integer> {

	public TimeCommand(PidDefinition pid) {
		super(pid);
	}

	@Override
	public Integer decode(PidDefinition pid, ConnectorResponse connectorResponse) {

		log.info("Decoding the message: {}", connectorResponse.getMessage());

		final Optional<String> answer = decodeRawMessage(getQuery(), connectorResponse);
		if (answer.isPresent()) {
			final Integer result = Integer.parseInt(answer.get(), 16);
			log.info("Decoded message: {} for: {}", result, connectorResponse.getMessage());
			return result;
		}
		return null;
	}
}
