package org.openobd2.core.command;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EngineTempCommand extends Mode1Command implements Converter<Integer> {

	public EngineTempCommand() {
		super("05", "Get engine temperature");
	}

	@Override
	public Integer convert(@NonNull String data) {
		if (data.length() > 4) {
			final String noWhiteSpaces = data.substring(4).replaceAll("\\s", "");
			final int decimal = Integer.parseInt(noWhiteSpaces, 16);
			int value = decimal - 40;
			log.debug("Engine temp is: {}", value);
			return value;
		}
		return null;
	}
}
