package org.openobd2.core.command.obd.mode1;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EngineRpmCommand extends Mode1Command<Integer> {

	public EngineRpmCommand() {
		super("0c", "Engine RPM");
	}

	@Override
	public Integer convert(@NonNull String raw) {
		if (isSuccessAnswerCode(raw)) {
			final String data = getRawAnswerData(raw);

			final int a = Integer.parseInt(data.substring(0, 2), 16);
			final int b = Integer.parseInt(data.substring(2, 4), 16);

			final int value = ((a * 256) + b) / 4;
			log.debug("Engine rpm is: {}", value);
			return value;
		}
		return null;
	}
}
