package org.openobd2.core;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

import org.openobd2.core.command.Command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class CommandsBuffer {

	private volatile LinkedBlockingDeque<Command> queue = new LinkedBlockingDeque<Command>();

	void addAll(Collection<? extends Command> commands) {
		// no synchronization need, already synchronized
		queue.addAll(commands);
	}
	
	<T extends Command>  void addFirst(T command) {
		// no synchronization need, already synchronized
		queue.addFirst(command);
	}
	
	<T extends Command>  void add(T command) {
		// no synchronization need, already synchronized
		queue.add(command);
	}

	<T extends Command>  T get() {
		// no synchronization need, already synchronized
		return (T) queue.poll();
	}

	boolean isEmpty() {
		return queue.isEmpty();
	}
}
