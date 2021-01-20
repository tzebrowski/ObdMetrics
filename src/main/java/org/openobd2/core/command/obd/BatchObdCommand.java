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
	
	public static void main(String[] args) {
		String aa = "00b0:410c000010001:000b660d000000";
		String normalized = aa.replaceAll("[a-zA-Z0-9]{1}\\:","");
		System.out.println(normalized);
	}
	
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

			final String normalized = message.replaceAll("[a-zA-Z0-9]{1}\\:","");
			final int indexOfAnswerCode = normalized.indexOf(predictedAnswerCode);
			
			if (indexOfAnswerCode == 0 || indexOfAnswerCode == 3) {
				int messageIndex = message.indexOf(predictedAnswerCode);
					
				pid_loop: for (final PidDefinition pid : pids) {
					for (; messageIndex < normalized.length(); messageIndex += 2) {
						int sizeOfPid = messageIndex + 2;
						final String sequence = normalized.substring(messageIndex, sizeOfPid).toUpperCase();

						if (sequence.equalsIgnoreCase(pid.getPid())) {
													final int endIndex = sizeOfPid + (pid.getLength() * 2) > normalized.length()
									? normalized.length()
									: sizeOfPid + (pid.getLength() * 2);
							String pidValue = normalized.substring(sizeOfPid, endIndex);
							final String value = predictedAnswerCode + sequence + pidValue;
							final ObdCommand command = commands.stream()
									.filter(p -> p.getPid().getPid().equals(sequence)).findFirst().get();

							values.put(command, value);
							messageIndex += (pid.getLength() * 2);
							continue pid_loop;
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
