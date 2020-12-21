package org.openelm327.core.command;

public final class ReadVoltagetCommand extends ATCommand {
	public ReadVoltagetCommand() {
		super("ATRV", "Read voltage");
	}
}
