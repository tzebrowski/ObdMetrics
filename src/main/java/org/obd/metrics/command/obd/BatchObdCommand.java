package org.obd.metrics.command.obd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obd.metrics.codec.MetricsDecoder;
import org.obd.metrics.codec.batch.Batchable;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchObdCommand extends ObdCommand implements Batchable {

	private final List<ObdCommand> commands;
	private final String predictedAnswerCode;

	public BatchObdCommand(String query, List<ObdCommand> commands) {
		super(query);
		this.commands = commands;
		this.predictedAnswerCode = new MetricsDecoder()
		        .getPredictedAnswerCode(commands.iterator().next().getPid().getMode());
	}

	@Override
	public Map<ObdCommand, String> decode(@NonNull String message) {
		var values = new HashMap<ObdCommand, String>();

		var normalized = message.replaceAll("[a-zA-Z0-9]{1}\\:", "");
		var indexOfAnswerCode = normalized.indexOf(predictedAnswerCode);

		if (indexOfAnswerCode == 0 || indexOfAnswerCode == 3) {
			var messageIndex = indexOfAnswerCode + 2;

			for (var command : commands) {
				if (messageIndex == normalized.length()) {
					break;
				}
				var pid = command.pid;
				var sizeOfPid = messageIndex + 2;
				var sequence = normalized.substring(messageIndex, sizeOfPid).toUpperCase();
				if (sequence.equalsIgnoreCase(pid.getPid())) {
					var pidLength = pid.getLength() * 2;
					var pidValue = normalized.substring(sizeOfPid, sizeOfPid + pidLength);
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
		var builder = new StringBuilder();
		builder.append("[query=");
		builder.append(query);
		builder.append("]");
		return builder.toString();
	}
}
