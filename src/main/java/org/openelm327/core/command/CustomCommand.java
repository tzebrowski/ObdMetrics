package org.openelm327.core.command;

public class CustomCommand extends Command {

	public CustomCommand(String command) {
		super(command, "Custom command: " + command);
	}

}
