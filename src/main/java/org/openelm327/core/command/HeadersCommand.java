package org.openelm327.core.command;

public final class HeadersCommand extends Command {
	public HeadersCommand(int value) {
		super("ATH" + value, "Headers");
	}
}
