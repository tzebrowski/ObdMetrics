package org.openobd2.core.command;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
abstract class ObdFrame extends Command {

	@Getter
	private String mode;

	@Getter
	private String pid;

	ObdFrame(final String mode, final String pid, final String label) {
		super(mode + pid, label);
		this.mode = mode;
		this.pid = pid;
	}
}
