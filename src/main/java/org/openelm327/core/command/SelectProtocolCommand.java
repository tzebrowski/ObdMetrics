package org.openelm327.core.command;

public final class SelectProtocolCommand extends ATCommand {
	public SelectProtocolCommand(int value) {
		super("AT SP" + value, "Select protocol");
	}
}
