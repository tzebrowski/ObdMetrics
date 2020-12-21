package org.openelm327.core.command;

public final class ProtocolCloseCommand extends ATCommand {
	public ProtocolCloseCommand() {
		super("ATPC", "Protocol close");
	}
}
