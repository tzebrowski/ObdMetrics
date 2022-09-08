package org.obd.metrics.command.meta;

import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class MetadataCommand extends Command {
	private static final String pattern = "[a-zA-Z0-9]{1}\\:";

	protected final PidDefinition pid;

	protected MetadataCommand(PidDefinition pid) {
		super(pid.getMode() + pid.getPid(), pid.getMode(), pid.getDescription());
		this.pid = pid;
	}

	protected String getNormalizedMessage(final String command, final String answer) {
		final String message = command.replaceAll(" ", "");
		final int leadingSuccessCodeNumber = message.charAt(0) + 4;
		final String successCode = (char) (leadingSuccessCodeNumber) + message.substring(1);
		final int indexOfSuccessCode = answer.indexOf(successCode);

		if (indexOfSuccessCode >= 0) {
			final String normalizedMsg = answer.substring(indexOfSuccessCode + successCode.length()).replaceAll(pattern,
					"");

			if (log.isTraceEnabled()) {
				log.trace("successCode= '{}', indexOfSuccessCode='{}',normalizedMsg='{}'", successCode,
						indexOfSuccessCode, normalizedMsg);
			}
			return normalizedMsg;
		} else {
			throw new IllegalArgumentException("Answer code is incorrect=" + successCode);
		}
	}
}
