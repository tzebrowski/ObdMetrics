package org.obd.metrics;

import org.obd.metrics.command.CommandReply;

import rx.Observer;

public abstract class CommandReplySubscriber implements Observer<CommandReply<?>> {
	@Override
	public void onCompleted() {
	}

	@Override
	public void onError(Throwable e) {

	}

	@Override
	public void onNext(CommandReply<?> t) {

	}
}
