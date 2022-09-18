package org.obd.metrics.executor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeCommand;

import lombok.Getter;

final class DiagnosticTroubleCodeReader extends ReplyObserver<Reply<?>> {

	@Getter
	private Set<String> codes = new HashSet<>();

	@Override
	public void onNext(Reply<?> reply) {
		final DiagnosticTroubleCodeCommand command = (DiagnosticTroubleCodeCommand) reply.getCommand();
		codes.addAll(command.decode(null, reply.getRaw()));
	}

	@Override
	public List<Class<?>> subscribeFor() {
		return Arrays.asList(DiagnosticTroubleCodeCommand.class);
	}
}
