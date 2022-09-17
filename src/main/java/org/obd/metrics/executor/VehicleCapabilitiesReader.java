package org.obd.metrics.executor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.command.SupportedPIDsCommand;

import lombok.Getter;

final class VehicleCapabilitiesReader extends ReplyObserver<Reply<?>> {

	@Getter
	private Set<String> capabilities = new HashSet<>();

	@Override
	public void onNext(Reply<?> reply) {
		final SupportedPIDsCommand command = (SupportedPIDsCommand) reply.getCommand();
		capabilities.addAll(command.decode(command.getPid(), reply.getRaw()));
	}

	@Override
	public List<Class<?>> subscribeFor() {
		return Arrays.asList(SupportedPIDsCommand.class);
	}
}
