package org.openobd2.core.command.obd;

import org.openobd2.core.command.Command;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
public abstract class ObdFrame extends Command {

	@Getter
	private String mode;

	@Getter
	private String pid;

	public ObdFrame(final String mode, final String pid, final String label) {
		super(mode + pid, label);
		this.mode = mode;
		this.pid = pid;
	}
}
