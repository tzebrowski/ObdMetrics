package org.obd.metrics.pid;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.SupportedPIDsCommand;
import org.obd.metrics.command.dtc.DtcCommand;
import org.obd.metrics.command.meta.HexCommand;

public enum PidGroup {

	LIVEDATA(null), METADATA(HexCommand.class), DTC(DtcCommand.class), CAPABILITES(SupportedPIDsCommand.class);

	private final Class<? extends Command> defaultCommandClass;

	PidGroup(Class<? extends Command> defaultCommand) {
		this.defaultCommandClass = defaultCommand;
	}

	public Class<? extends Command> getDefaultCommandClass() {
		return defaultCommandClass;
	}
}
