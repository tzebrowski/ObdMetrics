package org.openelm327.core.command;

public final class CustomCommand extends ATCommand {
	public CustomCommand(String command) {
		super(command, "Custom command: " + command);
	}
}
