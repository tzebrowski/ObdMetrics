package org.openobd2.core.command.at;

import org.openobd2.core.command.Command;

abstract class ATCommand extends Command {

	private static final String PREFIX = "AT";

	ATCommand(String query, String label) {
		super(PREFIX + query, label);
	}
	
	@Override
	public Long getDelayBeforeExecution() {
		return 700l;
	}
}
