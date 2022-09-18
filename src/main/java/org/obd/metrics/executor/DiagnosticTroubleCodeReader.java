package org.obd.metrics.executor;

import java.util.HashSet;
import java.util.Set;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeCommand;
import org.obd.metrics.pid.PIDsGroup;

final class DiagnosticTroubleCodeReader extends PIDsGroupReader<Set<String>> {

	DiagnosticTroubleCodeReader() {
		super(PIDsGroup.DTC);
		value = new HashSet<String>();
	}

	@Override
	public void onNext(Reply<?> reply) {
		final DiagnosticTroubleCodeCommand command = (DiagnosticTroubleCodeCommand) reply.getCommand();
		
		value.addAll(command.decode(null, reply.getRaw()));
	}
}
