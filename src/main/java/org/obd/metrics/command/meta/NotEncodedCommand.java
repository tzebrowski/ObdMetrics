package org.obd.metrics.command.meta;

import java.util.Optional;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class NotEncodedCommand extends MetadataCommand implements Codec<String> {

	public NotEncodedCommand(PidDefinition pid) {
		super(pid);
	}

	@Override
	public String decode(PidDefinition pid, RawMessage raw) {

		log.info("Decoding the message: {}", raw.getMessage());
		final Optional<String> answer = decodeRawMessage(getQuery(), raw);
		if (answer.isPresent()) {
			log.info("Decoded message: {} for: {}", answer.get(), raw.getMessage());
			return answer.get();
		}
		return null;
	}
}
