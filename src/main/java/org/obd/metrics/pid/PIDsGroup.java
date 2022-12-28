package org.obd.metrics.pid;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.SupportedPIDsCommand;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeClearCommand;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeCommand;
import org.obd.metrics.command.meta.HexCommand;

import lombok.Getter;

public enum PIDsGroup {

	LIVEDATA(null),
	METADATA(HexCommand.class), 
	DTC_READ(DiagnosticTroubleCodeCommand.class), 
	DTC_CLEAR(DiagnosticTroubleCodeClearCommand.class), 
	CAPABILITES(SupportedPIDsCommand.class);

	@Getter
	private final Class<? extends Command> defaultCommandClass;

	PIDsGroup(Class<? extends Command> defaultCommand) {
		this.defaultCommandClass = defaultCommand;
	}
}
