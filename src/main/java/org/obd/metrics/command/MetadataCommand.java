package org.obd.metrics.command;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;
import org.obd.metrics.transport.Characters;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MetadataCommand extends Command implements Codec<String> {

	private static final String pattern = "[a-zA-Z0-9]{1}\\:";
	private final PidDefinition pid;

	public MetadataCommand(PidDefinition pid) {
		super(pid.getMode() + pid.getPid(), pid.getMode(), pid.getDescription());
		this.pid = pid;
	}

	private String decode(final String command, final String answer) {
		final String message = command.replaceAll(" ", "");
		final int leadingSuccessCodeNumber = message.charAt(0) + 4;
		final String successCode = (char) (leadingSuccessCodeNumber) + message.substring(1);
		final int indexOfSuccessCode = answer.indexOf(successCode);

		if (indexOfSuccessCode >= 0) {
			final String normalizedMsg = 
					answer.substring(indexOfSuccessCode + successCode.length()).replaceAll(pattern,"");
			
			if (log.isTraceEnabled()) {
				log.trace("successCode= '{}', indexOfSuccessCode='{}',normalizedMsg='{}'", successCode,
					indexOfSuccessCode, normalizedMsg);
			}
			
			if (pid.getDecode()) {
				final String decoded = Hex.decode(normalizedMsg);
				return decoded == null ? null : decoded.trim();
			} else {
				return normalizedMsg;
			}
		} else {
			throw new IllegalArgumentException("Answer code is incorrect=" + successCode);
		}
	}

	@Override
	public String decode(PidDefinition pid, RawMessage raw) {

		log.info("Decoding the message: {}. Decode Flag={}", raw.getMessage(), this.pid.getDecode());

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
