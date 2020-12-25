package org.openobd2.core.command.at;

public final class SelectProtocolCommand extends ATCommand {
	public SelectProtocolCommand(int value) {
		super("SP" + value, "Select protocol");
	}
}
