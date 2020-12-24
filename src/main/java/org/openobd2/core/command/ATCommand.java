package org.openobd2.core.command;

abstract class ATCommand extends Command {

	private static final String PREFIX = "AT";

	public ATCommand(String query, String label) {
		super(PREFIX + query, label);
	}
}
