package org.openobd2.core.command.process;

import org.openobd2.core.command.Command;

public final class InitCompletedCommand extends Command {
	public InitCompletedCommand() {
		super("INIT_COMPLETED", "Init completed");
	}
}
