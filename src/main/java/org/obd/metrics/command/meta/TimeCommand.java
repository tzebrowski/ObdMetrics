package org.obd.metrics.command.meta;

import java.util.Optional;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TimeCommand extends MetadataCommand implements Codec<Integer> {

	public TimeCommand(PidDefinition pid) {
		super(pid);
	}

	@Override
	public Integer decode(PidDefinition pid, ConnectorMessage raw) {

		log.info("Decoding the message: {}", raw.getMessage());

		final Optional<String> answer = decodeRawMessage(getQuery(), raw);
		if (answer.isPresent()) {
			final Integer result = Integer.parseInt(answer.get(), 16);
			log.info("Decoded message: {} for: {}", result, raw.getMessage());
			return result;
		}
		return null;
	}
}
