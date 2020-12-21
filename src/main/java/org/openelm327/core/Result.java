package org.openelm327.core;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.openelm327.core.command.ATCommandResult;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class Result {

	volatile Queue<ATCommandResult> queue = new LinkedBlockingDeque<ATCommandResult>();

	void add(ATCommandResult command) {
		queue.add(command);
	}

	ATCommandResult get() {
		return queue.element();
	}

	boolean isEmpty() {
		return queue.isEmpty();
	}
}
