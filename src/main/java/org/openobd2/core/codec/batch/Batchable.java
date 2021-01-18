package org.openobd2.core.codec.batch;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.openobd2.core.command.obd.BatchObdCommand;
import org.openobd2.core.command.obd.ObdCommand;

import lombok.NonNull;

public interface Batchable {

	public static final int BATCH_SIZE = 6;

	default Map<ObdCommand, String> decode(@NonNull String message) {
		return new Hashtable<>();
	}

	default List<BatchObdCommand> encode(List<ObdCommand> commands) {
		return ListUtils.partition(commands, BATCH_SIZE).stream().map(partions -> {
			return new BatchObdCommand(
					partions.get(0).getPid().getMode() + " "
							+ partions.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" ")),
					commands);
		}).collect(Collectors.toList());
	}
}
