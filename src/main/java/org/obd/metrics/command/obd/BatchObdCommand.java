package org.obd.metrics.command.obd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obd.metrics.codec.MetricsDecoder;
import org.obd.metrics.codec.batch.Batchable;
import org.obd.metrics.pid.PidDefinition;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchObdCommand extends ObdCommand implements Batchable {

	private final List<ObdCommand> commands;
	private final String predictedAnswerCode;

	@Getter
	private final int priority;

	public BatchObdCommand(String query, List<ObdCommand> commands, int priority) {
		super(query);
		this.commands = commands;
		this.priority = priority;
		this.predictedAnswerCode = new MetricsDecoder()
		        .getPredictedAnswerCode(commands.iterator().next().getPid().getMode());
	}

	@Override
	public Map<ObdCommand, String> decode(String message) {
		Map<ObdCommand, String> values = new HashMap<ObdCommand, String>();

		final String normalized = message.replaceAll("[a-zA-Z0-9]{1}\\:", "");
		int indexOfAnswerCode = normalized.indexOf(predictedAnswerCode);

		if (indexOfAnswerCode == 0 || indexOfAnswerCode == 3) {
			int messageIndex = indexOfAnswerCode + 2;

			for (final ObdCommand command : commands) {
				if (messageIndex == normalized.length()) {
					break;
				}
				final PidDefinition pid = command.pid;
				final int sizeOfPid = messageIndex + 2;
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

		return values;
	}

	@Override
	public String toString() {
		return "[priority=" + priority + ", query=" + query + "]";
	}
}
