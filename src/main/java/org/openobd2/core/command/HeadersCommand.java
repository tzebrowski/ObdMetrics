package org.openobd2.core.command;

public final class HeadersCommand extends ATCommand {
	public HeadersCommand(int value) {
		super("H" + value, "Headers");
	}
}
