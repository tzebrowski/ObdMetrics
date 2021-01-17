package org.openobd2.core.workflow;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.openobd2.core.command.obd.ObdCommand;

public interface Batchable {

	public static final int BATCH_SIZE = 6;

	default List<ObdCommand> toBatch(List<ObdCommand> cmds) {
		return ListUtils.partition(cmds, BATCH_SIZE).stream().map(partions -> {
			String query = "";
			for (final ObdCommand command : partions) {
				query += command.getPid().getPid() + " ";
			}
			query = (partions.get(0).getPid().getMode() + " " + query).trim();
			return new ObdCommand(query);
		}).collect(Collectors.toList());
	}
}
