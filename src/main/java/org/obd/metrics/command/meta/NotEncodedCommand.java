package org.obd.metrics.command.meta;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;
import org.obd.metrics.transport.Characters;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class NotEncodedCommand extends MetadataCommand implements Codec<String> {

	public NotEncodedCommand(PidDefinition pid) {
		super(pid);
	}

	@Override
	public String decode(PidDefinition pid, RawMessage raw) {

		log.info("Decoding the message: {}", raw.getMessage());

		final String message = Characters.normalize(raw.getMessage());
		try {
			final String result = getNormalizedMessage(getQuery(), message);
			log.info("Decoded message: {} for: {}", result, raw.getMessage());
			return result;
		} catch (IllegalArgumentException e) {
			log.warn("Failed to decode message. Invalid answer code. Message:{}", message);
			return null;
		}
	}
}
