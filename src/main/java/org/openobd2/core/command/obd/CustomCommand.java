package org.openobd2.core.command.obd;

import org.openobd2.core.pid.PidDefinition;

import lombok.NonNull;

public final class CustomCommand extends ObdFrame {

	public CustomCommand(@NonNull PidDefinition pid) {
		super(pid);
	}
}
