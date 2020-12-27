package org.openobd2.core.command.obd;

import org.openobd2.core.command.Command;
import org.openobd2.core.pid.PidDefinition;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
public abstract class ObdFrame extends Command {

	@Getter
	protected PidDefinition pidDefinition;

	public ObdFrame(PidDefinition pidDefinition) {
		super(pidDefinition.getMode() + pidDefinition.getPid(), pidDefinition.getDescription());
		this.pidDefinition = pidDefinition;
	}

}
