package org.openobd2.core.command.obd.mode1;

import org.openobd2.core.command.obd.ObdFrame;
import org.openobd2.core.converter.Converter;

//Get current data (RPM, Speed, Fuel Level, Engine Load, etc)
public abstract class Mode1Command<T> extends ObdFrame implements Converter<T> {

	private static final String CURRENT_DIAGNOSTIC_DATA_MODE = "01";

	public Mode1Command(String pid, String label) {
		super(CURRENT_DIAGNOSTIC_DATA_MODE, pid.toLowerCase(), label);
	}

}
