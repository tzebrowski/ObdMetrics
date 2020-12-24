package org.openobd2.core.command;

import lombok.Getter;

abstract class OBDCommand extends Command {

	@Getter
	private String mode;

	public OBDCommand(String mode, String pid, String label) {
		super(mode + pid, label);
		this.mode = mode;
	}
}
