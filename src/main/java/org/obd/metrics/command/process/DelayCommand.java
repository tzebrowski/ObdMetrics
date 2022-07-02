package org.obd.metrics.command.process;

import org.obd.metrics.command.Command;

import lombok.Getter;

public final class DelayCommand extends Command {
	@Getter
	private final long delay;

	public DelayCommand(final long delay) {
		super("Delay", "Delay....");
		this.delay = delay;
	}
}
