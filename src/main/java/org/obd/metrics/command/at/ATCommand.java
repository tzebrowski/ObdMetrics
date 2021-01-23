package org.obd.metrics.command.at;

import org.obd.metrics.command.Command;

abstract class ATCommand extends Command {

	private static final String PREFIX = "AT";

	ATCommand(String query, String label) {
		super(PREFIX + query, label);
	}
}
