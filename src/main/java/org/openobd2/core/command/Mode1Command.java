package org.openobd2.core.command;

//Get current data (RPM, Speed, Fuel Level, Engine Load, etc)
abstract class Mode1Command<T> extends ObdFrame implements Converter<T> {

	private static final String CURRENT_DIAGNOSTIC_DATA_MODE = "01";

	public Mode1Command(String pid, String label) {
		super(CURRENT_DIAGNOSTIC_DATA_MODE, pid, label);
	}

	// this is not good place for this
	protected boolean isSuccessAnswerCode(String data) {
		// success code = 0x40 + mode + pid
		return data.startsWith(getPredictedAnswerCode());
	}

	// this is not good place for this
	protected String getPredictedAnswerCode() {
		// success code = 0x40 + mode + pid
		return String.valueOf(40 + Integer.valueOf(getMode())) + getPid();
	}

	// this is not good place for this
	protected String getAnswerData(String data) {
		// success code = 0x40 + mode + pid
		return data.substring(getPredictedAnswerCode().length());
	}

}
