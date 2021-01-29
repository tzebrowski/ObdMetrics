package org.obd.metrics.command.process;

import org.obd.metrics.command.Command;

import lombok.Getter;

public final class DelayCommand extends Command implements ProcessCommand {
	@Getter
	private final long delay;

	public DelayCommand(long delay) {
		super("Delay", "Delay....");
		this.delay = delay;
	}
}
