package org.openelm327.core.command;

public class ResetCommand extends ATCommand {

	public ResetCommand() {
		super("ATZ", "reset all");
	}

}
