package org.obd.metrics.codec.batch;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class IdGenerator {

	private static final int _10 = 10;
	private static final int _100000 = 10000;

	static long generate(final int length, final long pidId, int index, final byte[] bytes) {
		int postfix = 0;
		long prefix = pidId * _100000;

		if (length >= 1) {
			int digit = bytes[index];
			postfix *= _10;
			postfix += digit;

			digit = bytes[++index];
			postfix *= _10;
			postfix += digit;
		}

		if (length >= 2) {
			int digit = bytes[++index];
			postfix *= _10;
			postfix += digit;

			digit = bytes[++index];
			postfix *= _10;
			postfix += digit;
			prefix *= 10;
		}

		if (length >= 3) {
			int digit = bytes[++index];
			postfix *= _10;
			postfix += digit;

			digit = bytes[++index];
			postfix *= _10;
			postfix += digit;
			prefix *= 100;

		}

		if (length >= 4) {
			int digit = bytes[++index];
			postfix *= _10;
			postfix += digit;

			digit = bytes[++index];
			postfix *= _10;
			postfix += digit;
			prefix *= 100;
		}

		return prefix + postfix;
	}
}
