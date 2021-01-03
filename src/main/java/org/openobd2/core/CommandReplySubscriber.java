package org.openobd2.core;

import org.openobd2.core.command.CommandReply;

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
