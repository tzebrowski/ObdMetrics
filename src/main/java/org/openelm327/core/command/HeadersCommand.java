package org.openelm327.core.command;

public class HeadersCommand extends ATCommand {

	public HeadersCommand(int value) {
		super("ATH" + value, "Headers");
	}

}
