package org.openobd2.core.command;

public final class ResetCommand extends Command {
	public ResetCommand() {
		super("ATZ", "reset all");
	}
}
