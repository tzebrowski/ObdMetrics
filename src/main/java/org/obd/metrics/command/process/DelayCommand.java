package org.obd.metrics.command.process;

import org.obd.metrics.command.Command;

import lombok.Getter;
import lombok.Setter;

public final class DelayCommand extends Command {
	@Getter
	@Setter
	private long delay;

	public DelayCommand(final long delay) {
		super("Delay",null, "Delay....");
		this.delay = delay;
	}
}
