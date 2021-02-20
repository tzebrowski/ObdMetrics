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
		return ListUtils.partition(commands, BATCH_SIZE).stream().map(partions -> {
			return new BatchObdCommand(
			        partions.get(0).getPid().getMode() + " "
			                + partions.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" ")),
			        commands);
		}).collect(Collectors.toList());
	}

	Map<ObdCommand, String> decode(@NonNull String message);
}
