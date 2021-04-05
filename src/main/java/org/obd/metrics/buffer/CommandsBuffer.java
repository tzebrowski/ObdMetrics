package org.obd.metrics.buffer;

import java.util.Collection;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.group.CommandGroup;

public interface CommandsBuffer {

	CommandsBuffer clear();

	CommandsBuffer add(CommandGroup<?> group);

	CommandsBuffer addAll(Collection<? extends Command> commands);

	<T extends Command> CommandsBuffer addFirst(T command);

	<T extends Command> CommandsBuffer addLast(T command);

	Command get() throws InterruptedException;

	static CommandsBuffer instance() {
		return new DefaultCommandsBuffer();
	}
}