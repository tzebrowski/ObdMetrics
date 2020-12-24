package org.openobd2.core.command;

public final class SelectProtocolCommand extends ATCommand {
	public SelectProtocolCommand(int value) {
		super("SP" + value, "Select protocol");
	}
}
