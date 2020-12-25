package org.openobd2.core.command.at;

public final class HeadersCommand extends ATCommand {
	public HeadersCommand(int value) {
		super("H" + value, "Headers");
	}
}
