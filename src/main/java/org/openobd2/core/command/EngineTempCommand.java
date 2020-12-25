package org.openobd2.core.command;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EngineTempCommand extends Mode1Command<Long> {

	public EngineTempCommand() {
		super("05", "Engine Temperature");
	}

	@Override
	public Long convert(@NonNull String data) {
		if (isSuccessAnswerCode(data)) {
			final long decimal = getDecimalAnswerData(data);
			long value = decimal - 40;
			log.debug("Engine temp is: {}", value);
			return value;
		}
		return null;
	}
}
