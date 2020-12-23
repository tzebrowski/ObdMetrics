package org.openobd2.core.command;

public final class SelectProtocolCommand extends Command {
	public SelectProtocolCommand(int value) {
		super("ATSP" + value, "Select protocol");
	}
}
