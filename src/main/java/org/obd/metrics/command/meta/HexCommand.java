package org.obd.metrics.command.meta;

import java.util.Optional;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HexCommand extends MetadataCommand implements Codec<String> {

	public HexCommand(PidDefinition pid) {
		super(pid);
	}

	@Override
	public String decode(PidDefinition pid, ConnectorMessage raw) {

		log.info("Decoding the message: {}", raw.getMessage());
		final Optional<String> answer = decodeRawMessage(getQuery(),raw);

		if (answer.isPresent()) {
			final String decoded = Hex.decode(answer.get());
			final String result = (decoded == null) ? null : decoded.trim();
			log.info("Decoded message: {} for: {}", result, raw.getMessage());
			return result;
		}
		return null;
	}
}
