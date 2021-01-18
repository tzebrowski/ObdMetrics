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

	public BatchObdCommand(String query, List<ObdCommand> commands) {
		super(query);
		this.commands = commands;
	}

	@Override
	public Map<ObdCommand, String> decode(@NonNull String message) {
		final Map<ObdCommand, String> values = new HashMap<>();

		if (commands.size() == 0) {
			log.warn("No pids were specified");
		} else {

			final List<PidDefinition> pids = commands.stream().map(p -> p.getPid()).collect(Collectors.toList());

			final String mode = pids.iterator().next().getMode();
			final String predictedAnswerCode = new CommandReplyDecoder().getPredictedAnswerCode(mode);
			final int indexOf = message.indexOf(predictedAnswerCode);

			if (indexOf == 0 || indexOf == 5) {
				final String normalized = message.substring(indexOf + 2, message.length()).replace(":", "");
				final Map<String, Integer> pidLookupMap = pids.stream()
						.collect(Collectors.toMap(PidDefinition::getPid, item -> item.getLength()));

				for (int i = 0; i < normalized.length(); i++) {
					if (i + 2 < normalized.length()) {
						final String pid = normalized.substring(i, i + 2).toUpperCase();
						if (pidLookupMap.containsKey(pid)) {

							final String pidValue = normalized.substring(i + 2, i + 2 + (pidLookupMap.get(pid) * 2));
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
				log.warn("Answer code was not correct for message: {}", message);
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
