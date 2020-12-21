package org.openelm327.core.command;

public class EchoCommand extends ATCommand {
	public EchoCommand(int value) {
		super("ATE" + value, "Eacho");
	}
}
