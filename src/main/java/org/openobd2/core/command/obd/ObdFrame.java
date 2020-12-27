package org.openobd2.core.command.obd;

import org.openobd2.core.command.Command;
import org.openobd2.core.pid.PidDefinition;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
public abstract class ObdFrame extends Command {

	@Getter
	protected PidDefinition pid;

	public ObdFrame(PidDefinition pid) {
		super(pid.getMode() + pid.getPid(), pid.getDescription());
		this.pid = pid;
	}
}
