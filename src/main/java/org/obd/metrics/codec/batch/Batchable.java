package org.obd.metrics.codec.batch;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.NonNull;

public interface Batchable {
	static final int BATCH_SIZE = 6;

	static List<BatchObdCommand> encode(List<ObdCommand> commands) {
		if (commands.size() <= BATCH_SIZE) {
			// no splitting into groups, fetch all pids at once
			return ListUtils.partition(commands, BATCH_SIZE).stream().map(partitions -> {
				return map(partitions, 0);
			}).collect(Collectors.toList());
		} else {

			final Map<Integer, List<ObdCommand>> groupedByPriority = commands.stream()
			        .collect(Collectors.groupingBy(p -> p.getPid().getPriority()));
			
			return groupedByPriority.entrySet().stream().map(entry -> {
				//split by partitions of $BATCH_SIZE size commands
				return ListUtils.partition(entry.getValue(), BATCH_SIZE).stream().map(partitions -> {
					return map(partitions, entry.getKey());
				}).collect(Collectors.toList());
			}).flatMap(List::stream).collect(Collectors.toList());
		}
	}

	static BatchObdCommand map(List<ObdCommand> commands, int priority) {
		return new BatchObdCommand(
		        commands.get(0).getPid().getMode() + " "
		                + commands.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" ")),
		        commands, priority);
	}

	Map<ObdCommand, String> decode(@NonNull String message);
}
