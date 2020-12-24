package org.openobd2.core.command;

public final class CustomCommand extends CurrentDataCommand {
	
	public CustomCommand(String pid) {
		super(pid, "Custom command: " + pid);
	}
}
