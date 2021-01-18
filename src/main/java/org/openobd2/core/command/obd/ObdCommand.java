package org.openobd2.core.command.obd;

import org.openobd2.core.command.Command;
import org.openobd2.core.pid.PidDefinition;

import lombok.Getter;

public class ObdCommand extends Command {

	@Getter
	protected PidDefinition pid;

	public ObdCommand(String query) {
		super(query, "Custom: " + query);
	}

	public ObdCommand(PidDefinition pid) {
		super(pid.getMode() + pid.getPid(), pid.getDescription());
		this.pid = pid;
	}

	@Override
	public String toString() {

		final StringBuilder builder = new StringBuilder();
		builder.append("[pid=");
		if (pid != null) {
			builder.append(pid.getDescription());
		}

		builder.append(", query=");
		builder.append(query);
		builder.append("]");
		return builder.toString();
	}
}
