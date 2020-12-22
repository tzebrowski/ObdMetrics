package org.openelm327.core;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.openelm327.core.command.Command;
import org.openelm327.core.command.CommandReply;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class CommandReplyCollector implements Subscriber<CommandReply> {

	@Getter
	private MultiValuedMap<Command, CommandReply> data = new ArrayListValuedHashMap<Command, CommandReply>();
	private Subscription subscription;

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}

	@Override
	public void onNext(CommandReply item) {
		log.debug("Receive data: {}", item);
		data.put(item.getCommand(), item);
		this.subscription.request(1);
	}

	@Override
	public void onError(Throwable throwable) {
	}

	@Override
	public void onComplete() {
	}
}
