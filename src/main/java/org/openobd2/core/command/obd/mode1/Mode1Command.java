package org.openobd2.core.command.obd.mode1;

import org.openobd2.core.command.obd.ObdFrame;
import org.openobd2.core.converter.Converter;

//Get current data (RPM, Speed, Fuel Level, Engine Load, etc)
public abstract class Mode1Command<T> extends ObdFrame implements Converter<T> {

	private static final String CURRENT_DIAGNOSTIC_DATA_MODE = "01";

	public Mode1Command(String pid, String label) {
		super(CURRENT_DIAGNOSTIC_DATA_MODE, pid.toLowerCase(), label);
	}

	// this is not good place for this
	protected boolean isSuccessAnswerCode(String raw) {
		// success code = 0x40 + mode + pid
		return raw.toLowerCase().startsWith(getPredictedAnswerCode());
	}

	// this is not good place for this
	protected String getPredictedAnswerCode() {
		// success code = 0x40 + mode + pid
		return String.valueOf(40 + Integer.valueOf(getMode())) + getPid();
	}

	// this is not good place for this
	protected String getRawAnswerData(String raw) {
		// success code = 0x40 + mode + pid
		return raw.substring(getPredictedAnswerCode().length());
	}

	// this is not good place for this
	protected Long getDecimalAnswerData(String raw) {
		// success code = 0x40 + mode + pid
		return Long.parseLong(getRawAnswerData(raw), 16);
	}
}
