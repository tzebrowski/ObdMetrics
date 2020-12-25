package org.openobd2.core.command.obd.mode1;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class VehicleSpeedCommand extends Mode1Command<Long> {

	public VehicleSpeedCommand() {
		super("0D", "Vehicle speed");
	}

	@Override
	public Long convert(@NonNull String data) {
		if (isSuccessAnswerCode(data)) {
			final long speed = getDecimalAnswerData(data);
			log.debug("Vehicle speed is: {}", speed);
			return speed;
		}
		return null;
	}
}
