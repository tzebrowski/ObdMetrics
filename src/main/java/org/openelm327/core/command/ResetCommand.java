package org.openelm327.core.command;

public final class ResetCommand extends ATCommand {
	public ResetCommand() {
		super("ATZ", "reset all");
	}
}
