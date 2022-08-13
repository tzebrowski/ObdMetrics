package org.obd.metrics.command.obd;

import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = { "pid" }, callSuper = false)
public class ObdCommand extends Command {

	@Getter
	protected PidDefinition pid;

	public ObdCommand(final String query) {
		super(query, "Custom: " + query);
	}

	public ObdCommand(final PidDefinition pid) {
		super(pid.getMode() + pid.getPid(), pid.getDescription());
		this.pid = pid;
	}

	public int getPriority() {
		return pid.getPriority();
	}
	
	public String getMode() {
		return pid.getMode();
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
