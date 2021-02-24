package org.obd.metrics.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Hex {

	static String decode(String message) {
		final char[] data = message.toCharArray();
		if (data.length % 2 != 0) {
			log.warn("Incorrect hex");
			return null;
		}

		final char[] out = new char[data.length >> 1];
		final int len = data.length;
		for (int i = 0, j = 0; j < len; i++) {
			int f = Character.digit(data[j], 16) << 4;
			j++;
			f = f | Character.digit(data[j], 16);
			j++;
			out[i] = (char) (f & 0xFF);
		}
		return String.valueOf(out);
	}
}
