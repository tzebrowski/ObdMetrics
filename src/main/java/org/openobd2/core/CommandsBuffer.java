package org.openobd2.core;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandSet;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class CommandsBuffer {

	// no synchronization need, already synchronized
	private volatile LinkedBlockingDeque<Command> queue = new LinkedBlockingDeque<Command>();

	void add(CommandSet<?> commandSet) {
		addAll(commandSet.getCommands());
	}

	void addAll(Collection<? extends Command> commands) {
		queue.addAll(commands);
	}

	<T extends Command> void addFirst(T command) {
		queue.addFirst(command);
	}

	<T extends Command> void add(T command) {
		queue.add(command);
	}

	<T extends Command> T get() {
		return (T) queue.poll();
	}

	boolean isEmpty() {
		return queue.isEmpty();
	}
}
