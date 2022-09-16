package org.obd.metrics.command.meta;

import java.util.Optional;

import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.raw.RawMessage;
import org.obd.metrics.transport.Characters;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class MetadataCommand extends Command {
	private static final String pattern = "[a-zA-Z0-9]{1}\\:";

	protected final PidDefinition pid;

	protected MetadataCommand(PidDefinition pid) {
		super(pid.getMode() + pid.getPid(), pid.getMode(), pid.getDescription());
		this.pid = pid;
	}

	protected Optional<String> decodeRawMessage(final String command, final RawMessage raw) {
		final String message = command.replaceAll(" ", "");
		final int leadingSuccessCodeNumber = message.charAt(0) + 4;
		final String successCode = (char) (leadingSuccessCodeNumber) + message.substring(1);
		
		final String normazlizedAnswer = Characters.normalize(raw.getMessage());
		final int indexOfSuccessCode = normazlizedAnswer.indexOf(successCode);

		if (indexOfSuccessCode >= 0) {
			final String normalizedMsg = normazlizedAnswer.substring(indexOfSuccessCode + successCode.length()).replaceAll(pattern,
					"");

			if (log.isTraceEnabled()) {
				log.trace("successCode= '{}', indexOfSuccessCode='{}',normalizedMsg='{}'", successCode,
						indexOfSuccessCode, normalizedMsg);
			}
			return Optional.of(normalizedMsg);
		} else {
			log.warn("Failed to decode message. Invalid answer code. Message:{}", message);
			return Optional.empty();
		}
	}
}
