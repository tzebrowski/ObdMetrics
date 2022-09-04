package org.obd.metrics.command;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;
import org.obd.metrics.transport.Characters;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MetadataCommand extends Command implements Codec<String> {
	private static final String pattern = "[a-zA-Z0-9]{1}\\:";

	public MetadataCommand(PidDefinition pid) {
		super(pid.getMode() + pid.getPid(), pid.getMode(), pid.getDescription());
	}

	private String decode(final String command, final String answer) {
		final String message = command.replaceAll(" ", "");
		final int successAnswerCode = message.charAt(0) + 4;
		final String s = (char) (successAnswerCode) + message.substring(1);
		final int indexOfAnswerCode = answer.indexOf(s);

		if (indexOfAnswerCode > 0) {
			final String decoded = Hex.decode(answer.substring(indexOfAnswerCode + s.length()).replaceAll(pattern, ""));
			return decoded == null ? null : decoded.trim();
		} else {
			throw new IllegalArgumentException("Answer code is incorrect=" + s);
		}
	}

	@Override
	public String decode(PidDefinition pid, RawMessage raw) {
		log.info("Decoding the message: {}", raw.getMessage());
		final String message = Characters.normalize(raw.getMessage());
		try {
			final String result = decode(getQuery(), message);
			log.info("Decoded message: {} for: {}", result, raw.getMessage());
			return result;
		} catch (IllegalArgumentException e) {
			log.warn("Failed to decode message. Invalid answer code. Message:{}", message);
			return null;
		}
	}
}
