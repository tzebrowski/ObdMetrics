package org.openobd2.core;

import org.openobd2.core.command.CommandReply;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import rx.Observer;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
abstract class CommandReplySubscriber implements Observer<CommandReply<?>> {
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
