package org.openobd2.core.command.obd.mode1;

import org.openobd2.core.codec.Codec;
import org.openobd2.core.command.obd.ObdFrame;
import org.openobd2.core.pid.PidDefinition;

//Get current data (RPM, Speed, Fuel Level, Engine Load, etc)
public abstract class Mode1Command<T> extends ObdFrame implements Codec<T> {

	public Mode1Command(PidDefinition definition) {
		super(definition);
	}


	public String getMode() {
		return "01";
	}


}
