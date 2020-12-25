package org.openobd2.core.command.obd.mode1;

public final class CustomCommand extends Mode1Command<Object> {

	public CustomCommand(String pid) {
		super(pid, "Custom command: " + pid);
	}
}
