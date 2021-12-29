package org.obd.metrics.command;

public class ATCommand extends Command {

	private static final String PREFIX = "AT";
	
	public ATCommand(String query) {
		super(PREFIX + query,"AT Command: "  + query);
	}
}
