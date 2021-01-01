package org.openobd2.core;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

import org.openobd2.core.command.Command;
import org.openobd2.core.command.group.CommandGroup;

import lombok.Builder;
import lombok.Builder.Default;

@Builder
public final class CommandsBuffer {

	// no synchronization need, already synchronized
	@Default
	private volatile LinkedBlockingDeque<Command> queue = new LinkedBlockingDeque<Command>();

	public CommandsBuffer add(CommandGroup<?> group) {
		addAll(group.getCommands());
		return this;
	}

	public CommandsBuffer addAll(Collection<? extends Command> commands) {
		queue.addAll(commands);
		return this;

	}

	public <T extends Command> CommandsBuffer addFirst(T command) {
		queue.addFirst(command);
		return this;

	}

	public <T extends Command> CommandsBuffer add(T command) {
		queue.add(command);
		return this;

	}

	public Command get() {
		return queue.poll();
	}

	boolean isEmpty() {
		return queue.isEmpty();
	}
}
