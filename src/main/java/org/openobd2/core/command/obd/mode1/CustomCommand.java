package org.openobd2.core.command.obd.mode1;

import org.openobd2.core.pid.PidDefinition;

import lombok.NonNull;

public final class CustomCommand extends Mode1Command<Long> {

	public CustomCommand(@NonNull PidDefinition definition) {
		super(definition);
	}
}
