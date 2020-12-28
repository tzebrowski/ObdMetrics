package org.openobd2.core;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.openobd2.core.command.Command;
import org.openobd2.core.command.CommandReply;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class DataCollector extends CommandReplySubscriber {

	@Getter
	private MultiValuedMap<Command, CommandReply<?>> data = new ArrayListValuedHashMap<Command, CommandReply<?>>();

	@Override
	public void onNext(CommandReply<?> reply) {
		log.debug("Receive data: {}", reply);
		data.put(reply.getCommand(), reply);
		subscription.request(1);
	}
}
