package org.openelm327.core;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.openelm327.core.command.ATCommand;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class Commands {

	volatile Queue<ATCommand> queue = new LinkedBlockingDeque<ATCommand>();

	void add(ATCommand command) {
		queue.add(command);
	}

	ATCommand get() {
		return queue.poll();
	}

	boolean isEmpty() {
		return queue.isEmpty();
	}
}
