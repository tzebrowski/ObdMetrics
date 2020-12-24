package org.openobd2.core.command;

public final class CustomCommand extends Mode1Command {
	
	public CustomCommand(String pid) {
		super(pid, "Custom command: " + pid);
	}
}
