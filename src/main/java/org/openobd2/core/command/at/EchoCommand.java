package org.openobd2.core.command.at;

public class EchoCommand extends ATCommand {
	public EchoCommand(int value) {
		super("E" + value, "Echo command");
	}
}
