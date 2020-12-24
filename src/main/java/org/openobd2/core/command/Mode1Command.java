package org.openobd2.core.command;

//Get current data (RPM, Speed, Fuel Level, Engine Load, etc)
abstract class Mode1Command extends ObdFrame {

	private static final String CURRENT_DIAGNOSTIC_DATA_MODE = "01";

	public Mode1Command(String pid, String label) {
		super(CURRENT_DIAGNOSTIC_DATA_MODE, pid, label);
	}
}
