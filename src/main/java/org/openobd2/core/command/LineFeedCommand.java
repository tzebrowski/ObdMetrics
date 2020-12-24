package org.openobd2.core.command;

public final class LineFeedCommand extends ATCommand {
	public LineFeedCommand(int value) {
		super("L" + value, "Line feed command");
	}
}
