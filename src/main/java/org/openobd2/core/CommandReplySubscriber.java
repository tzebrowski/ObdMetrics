package org.openobd2.core;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import org.openobd2.core.command.CommandReply;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
abstract class CommandReplySubscriber implements Subscriber<CommandReply<?>> {
	protected Subscription subscription;

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}

	@Override
	public void onError(Throwable throwable) {
	}

	@Override
	public void onComplete() {
	}
}
