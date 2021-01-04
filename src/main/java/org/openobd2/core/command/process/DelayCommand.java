package org.openobd2.core.command.process;

import org.openobd2.core.command.Command;

import lombok.Getter;

public final class DelayCommand extends Command {
	@Getter
	private final long delay;

	public DelayCommand(long delay) {
		super("Delay", "Delay....");
		this.delay = delay;
	}
}
