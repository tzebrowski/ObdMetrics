package org.obd.metrics.executor;

import org.obd.metrics.api.model.Reply;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeClearCommand;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeClearStatus;
import org.obd.metrics.pid.PIDsGroup;

final class DiagnosticTroubleCodeCleaner extends PIDsGroupReader<DiagnosticTroubleCodeClearStatus> {

	DiagnosticTroubleCodeCleaner() {
		super(PIDsGroup.DTC_CLEAR);
		this.value = DiagnosticTroubleCodeClearStatus.NO_DATA;
	}

	@Override
	public void onNext(Reply<?> reply) {
		final DiagnosticTroubleCodeClearCommand command = (DiagnosticTroubleCodeClearCommand) reply.getCommand();
		value = command.decode(reply.getRaw());
	}
}
