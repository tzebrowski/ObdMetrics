package org.openobd2.core.command;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
abstract class OBDCommand extends Command {

	@Getter
	private String mode;
	
	@Getter
	private String pid;
	
	OBDCommand(String mode, String pid, String label) {
		super(mode + pid, label);
		this.mode = mode;
		this.pid  = pid;
	}
}
