package org.openobd2.core.command.obd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openobd2.core.codec.CommandReplyDecoder;
import org.openobd2.core.codec.batch.Batchable;
import org.openobd2.core.pid.PidDefinition;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchObdCommand extends ObdCommand implements Batchable {

	private final List<ObdCommand> commands;
	private final String predictedAnswerCode;

	public BatchObdCommand(String query, List<ObdCommand> commands) {
		super(query);
		this.commands = commands;
		this.predictedAnswerCode = new CommandReplyDecoder()
				.getPredictedAnswerCode(commands.iterator().next().getPid().getMode());
	}

	@Override
	public Map<ObdCommand, String> decode(@NonNull String message) {
		final Map<ObdCommand, String> values = new HashMap<>();

		if (commands.size() == 0) {
			log.warn("No pids were specified");
		} else {

			final String normalized = message.replaceAll("[a-zA-Z0-9]{1}\\:", "");
			final int indexOfAnswerCode = normalized.indexOf(predictedAnswerCode);

			if (indexOfAnswerCode == 0 || indexOfAnswerCode == 3) {
				int messageIndex = indexOfAnswerCode + 2;

				for (final ObdCommand command : commands) {
					final PidDefinition pid = command.pid;
					int sizeOfPid = messageIndex + 2;
					if (sizeOfPid > normalized.length()) {
						sizeOfPid = normalized.length();
					}

					final String sequence = normalized.substring(messageIndex, sizeOfPid).toUpperCase();
					if (sequence.equalsIgnoreCase(pid.getPid())) {
						final int pidLength = pid.getLength() * 2;
						final String pidValue = normalized.substring(sizeOfPid, sizeOfPid + pidLength);
						values.put(command, predictedAnswerCode + sequence + pidValue);
						messageIndex += pidLength + 2;
						continue;
					}
				}
				return values;
			} else {
				log.warn("Answer code was not correct for message: {}. Query: {}", message, query);
			}
		}
		return values;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[query=");
		builder.append(query);
		builder.append("]");
		return builder.toString();
	}
}
