package org.openobd2.core;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.openobd2.core.command.Command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class CommandsBuffer {

	private volatile LinkedBlockingDeque<Command> queue = new LinkedBlockingDeque<Command>();

	void addAll(List<? extends Command> commands) {
		// no synchronization need, already synchronized
		queue.addAll(commands);
	}
	
	void addFirst(Command command) {
		// no synchronization need, already synchronized
		queue.addFirst(command);
	}
	
	void add(Command command) {
		// no synchronization need, already synchronized
		queue.add(command);
	}

	Command get() {
		// no synchronization need, already synchronized
		return queue.poll();
	}

	boolean isEmpty() {
		return queue.isEmpty();
	}
}
