package org.openobd2.core.command;

public class EchoCommand extends Command {
	public EchoCommand(int value) {
		super("ATE" + value, "Eacho");
	}
}
