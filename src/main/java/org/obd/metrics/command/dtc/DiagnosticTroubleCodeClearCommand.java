package org.obd.metrics.command.dtc;

import org.obd.metrics.codec.Codec;
import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DiagnosticTroubleCodeClearCommand extends Command
		implements Codec<DiagnosticTroubleCodeClearStatus> {

	protected final PidDefinition pid;

	public DiagnosticTroubleCodeClearCommand(PidDefinition pid) {
		super(pid.getQuery(), pid.getMode(), pid.getDescription());
		this.pid = pid;
	}

	@Override
	public DiagnosticTroubleCodeClearStatus decode(final PidDefinition pidDef,
			final ConnectorResponse connectorResponse) {

		final String message = connectorResponse.getMessage();

		log.info("Received following response for DTC Clear operation: {}", connectorResponse.getMessage());
		if (message.startsWith(pid.getSuccessCode())) {
			log.debug("Operation of DTC cleaning completed successfully");
			return DiagnosticTroubleCodeClearStatus.OK;
		} else {
			log.debug("Operation of DTC cleaning failed.");
			return DiagnosticTroubleCodeClearStatus.ERR;
		}
	}
}
