package org.openobd2.core.command;

//Get current data (RPM, Speed, Fuel Level, Engine Load, etc)
abstract class Mode1Command extends ObdFrame {

	private static final String CURRENT_DIAGNOSTIC_DATA_MODE = "01";

	public Mode1Command(String pid, String label) {
		super(CURRENT_DIAGNOSTIC_DATA_MODE, pid, label);
	}

	//this is not good place for this 
	protected boolean isSuccessAnswerCode(String data) {
		// success code = 0x40 + mode + pid
		return data.startsWith(String.valueOf(40 + Integer.valueOf(getMode()) + getPid()));
	}
}
