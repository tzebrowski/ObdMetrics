package org.obd.metrics.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Hex {

	static char[] decode(final char[] data) {
		final char[] out = new char[data.length >> 1];
		final int len = data.length;
		for (int i = 0, j = 0; j < len; i++) {
			int f = Character.digit(data[j], 16) << 4;
			j++;
			f = f | Character.digit(data[j], 16);
			j++;
			out[i] = (char) (f & 0xFF);
		}
		return out;
	}
}
