package org.obd.metrics;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.CommandReplySubscriber;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.CommandReply;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class DataCollector extends CommandReplySubscriber {

	@Getter
	private MultiValuedMap<Command, CommandReply<?>> data = new ArrayListValuedHashMap<Command, CommandReply<?>>();

	@Override
	public void onNext(CommandReply<?> reply) {
		log.info("Receive data: {}", reply);
		data.put(reply.getCommand(), reply);
	}
}
