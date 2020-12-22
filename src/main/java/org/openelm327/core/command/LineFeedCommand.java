package org.openelm327.core.command;

public final class LineFeedCommand extends Command {
	public LineFeedCommand(int value) {
		super("AT L" + value, "Line feed command");
	}
}
