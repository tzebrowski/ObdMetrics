package org.openobd2.core.command.obd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openobd2.core.codec.CommandReplyDecoder;
import org.openobd2.core.codec.batch.Batchable;
import org.openobd2.core.pid.PidDefinition;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchObdCommand extends ObdCommand implements Batchable {

	private final List<ObdCommand> commands;
	private final List<PidDefinition> pids;
	private final String predictedAnswerCode;

	public BatchObdCommand(String query, List<ObdCommand> commands) {
		super(query);

		this.commands = commands;
		this.pids = commands.stream().map(p -> p.getPid()).collect(Collectors.toList());
		final String mode = pids.iterator().next().getMode();
		this.predictedAnswerCode = new CommandReplyDecoder().getPredictedAnswerCode(mode);
	}

	@Override
	public Map<ObdCommand, String> decode(@NonNull String message) {
		final Map<ObdCommand, String> values = new HashMap<>();

		if (commands.size() == 0) {
			log.warn("No pids were specified");
		} else {

			final int indexOf = message.indexOf(predictedAnswerCode);

			if (indexOf == 0 || indexOf == 5) {
				final String normalized = message.substring(indexOf + 2, message.length()).replace(":", "");
				final Map<String, Integer> pidLookupMap = pids.stream()
						.collect(Collectors.toMap(PidDefinition::getPid, item -> item.getLength()));

				for (int i = 0; i < normalized.length(); i++) {
					final int sizeOfPid = i + 2;
					if (sizeOfPid < normalized.length()) {
						final String pid = normalized.substring(i, sizeOfPid).toUpperCase();
						if (pidLookupMap.containsKey(pid)) {

							int endIndex = sizeOfPid + (pidLookupMap.get(pid) * 2);
							if (endIndex > normalized.length()) {
								endIndex = normalized.length();
							}
							final String pidValue = normalized.substring(sizeOfPid,
									endIndex);
							pidLookupMap.remove(pid);

							final String value = predictedAnswerCode + pid + pidValue;
							final ObdCommand command = commands.stream().filter(p -> p.getPid().getPid().equals(pid))
									.findFirst().get();

							values.put(command, value);
						}
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
