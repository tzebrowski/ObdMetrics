package org.openobd2.core.command;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EngineTempCommand extends Command implements Converter<Integer> {

	public EngineTempCommand() {
		super("0105", "Get engine temperature");
	}

	@Override
	public Integer convert(@NonNull String data) {
		if (data.length() > 6) {
			final String noWhiteSpaces = data.substring(6).replaceAll("\\s", "");
			final int decimal = Integer.parseInt(noWhiteSpaces, 16);
			int value = decimal - 40;
			log.debug("Engine temp is: {}", value);
			return value;
		}
		return null;
	}
}