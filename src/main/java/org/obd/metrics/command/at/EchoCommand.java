package org.obd.metrics.command.at;

public class EchoCommand extends ATCommand {
	public EchoCommand(int value) {
		super("E" + value, "Echo command");
	}
}
