package org.obd.metrics.command.process;

import org.obd.metrics.command.Command;

public final class InitCompletedCommand extends Command implements ProcessCommand {
	public InitCompletedCommand() {
		super("INIT_COMPLETED", "Init completed");
	}
}
