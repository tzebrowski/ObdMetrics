package org.openelm327.core;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.openelm327.core.command.Command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class Commands {

	volatile Queue<Command> queue = new LinkedBlockingDeque<Command>();

	void add(Command command) {
		queue.add(command);
	}

	Command get() {
		return queue.poll();
	}

	boolean isEmpty() {
		return queue.isEmpty();
	}
}
