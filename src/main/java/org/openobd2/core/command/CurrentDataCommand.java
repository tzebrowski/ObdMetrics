package org.openobd2.core.command;

abstract class CurrentDataCommand extends OBDCommand {

	private static final String CURRENT_DIAGNOSTIC_DATA_MODE = "01";

	public CurrentDataCommand(String pid, String label) {
		super(CURRENT_DIAGNOSTIC_DATA_MODE, pid, label);
	}
}
