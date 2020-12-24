package org.openobd2.core.command;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EngineTempCommand extends Mode1Command<Integer> {

	public EngineTempCommand() {
		super("05", "Get engine temperature");
	}

	@Override
	public Integer convert(@NonNull String data) {
		if (isSuccessAnswerCode(data)) {
			final String noWhiteSpaces = getAnswerData(data);
			final int decimal = Integer.parseInt(noWhiteSpaces, 16);
			int value = decimal - 40;
			log.debug("Engine temp is: {}", value);
			return value;
		}
		return null;
	}
}
