package org.obd.metrics.raw;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class IdGenerator {

	private static final int _10 = 10;
	private static final int _100000 = 10000000;

	public static long generateId(final int length,final Long pidId,final int startIndex, byte[] bytes) {
		return (pidId * _100000) + convert(length, startIndex, bytes);
	}

	private static int convert(int length, int index, byte[] bytes) {
		int result = 0;
		if (length >= 1) {
			int digit = bytes[index + 0];
			result *= _10;
			result += digit;

			digit = bytes[index + 1];
			result *= _10;
			result += digit;
		}
		if (length >= 2) {
			int digit = bytes[index + 2];
			result *= _10;
			result += digit;

			digit = bytes[index + 3];
			result *= _10;
			result += digit;
		}

		if (length >= 3) {
			int digit = bytes[index + 4];
			result *= _10;
			result += digit;

			digit = bytes[index + 5];
			result *= _10;
			result += digit;
		}
		return result;
	}
}
