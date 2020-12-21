package org.openelm327.core;

import java.util.Queue;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.LinkedBlockingDeque;

import org.openelm327.core.command.CommandResult;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class CommandResultSubscriber implements Subscriber<CommandResult> {

	private Queue<CommandResult> queue = new LinkedBlockingDeque<CommandResult>();
	private Flow.Subscription subscription;

	void add(CommandResult command) {
		queue.add(command);
	}

	CommandResult get() {
		return queue.element();
	}

	boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}

	@Override
	public void onNext(CommandResult item) {
		log.info("Receive command result: {}", item);
		queue.add(item);
		this.subscription.request(1);
	}

	@Override
	public void onError(Throwable throwable) {
	}

	@Override
	public void onComplete() {
	}
}
