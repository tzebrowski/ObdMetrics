package org.openelm327.core;

import java.util.Queue;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.LinkedBlockingDeque;

import org.openelm327.core.command.CommandReply;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class CommandReplyCollector implements Subscriber<CommandReply> {

	private Queue<CommandReply> queue = new LinkedBlockingDeque<CommandReply>();
	private Flow.Subscription subscription;

	void add(CommandReply reply) {
		queue.add(reply);
	}

	CommandReply get() {
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
	public void onNext(CommandReply item) {
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
