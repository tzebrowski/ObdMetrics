package org.openobd2.core.command;

abstract class Mode1Command extends OBDCommand {

	private static final String CURRENT_DIAGNOSTIC_DATA_MODE = "01";

	public Mode1Command(String pid, String label) {
		super(CURRENT_DIAGNOSTIC_DATA_MODE, pid, label);
	}
}
