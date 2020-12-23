package org.openobd2.core.command;

public final class LineFeedCommand extends Command {
	public LineFeedCommand(int value) {
		super("ATL" + value, "Line feed command");
	}
}
