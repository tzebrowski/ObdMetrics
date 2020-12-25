package org.openobd2.core.command.at;

public final class ResetCommand extends ATCommand {
	public ResetCommand() {
		super("Z", "reset all");
	}
}
