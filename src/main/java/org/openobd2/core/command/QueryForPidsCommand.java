package org.openobd2.core.command;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class QueryForPidsCommand extends Command implements Converter<List<String>> {

	public QueryForPidsCommand(String value) {
		super("01 " + value, "Get supported pids");
	}

	@Override
	public List<String> convert(@NonNull String data) {
		final List<String> supportedPids = new ArrayList<String>();
		if (data.length() > 6) {
			final String noWhiteSpaces = data.substring(6).replaceAll("\\s", "");
			final String binStr = Long.toBinaryString(Long.parseLong(noWhiteSpaces, 16));
			for (int idx = 0; idx < binStr.length(); idx++) {
				if ('1' == binStr.charAt(idx)) {
					String hexString = Integer.toHexString((idx + 1));
					if (hexString.length() == 1) {
						hexString = "0" + hexString;
					}
					supportedPids.add(hexString);
				}
			}
		} else {
			log.warn("Failed to transform data: {}", data);
		}
		return supportedPids;
	}
}
