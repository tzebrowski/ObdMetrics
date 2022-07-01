package org.obd.metrics.codec.batch;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class IdGenerator {

	private static final int _10 = 10;
	private static final int _100000 = 10000;

	static long generate(final int length, final long pidId, int index, final byte[] bytes) {
		int postfix = 0;
		long prefix = pidId * _100000;

		if (length >= 1 && bytes.length >= index + 1) {
			int digit = bytes[index];
			postfix *= _10;
			postfix += digit;

			digit = bytes[++index];
			postfix *= _10;
			postfix += digit;
		}

		if (length >= 2 && bytes.length >= index + 2) {
			int digit = bytes[++index];
			postfix *= _10;
			postfix += digit;

			digit = bytes[++index];
			postfix *= _10;
			postfix += digit;
			prefix *= 10;
		}

		if (length >= 3 && bytes.length >= index + 2) {
			int digit = bytes[++index];
			postfix *= _10;
			postfix += digit;

			digit = bytes[++index];
			postfix *= _10;
			postfix += digit;
			prefix *= 100;

		}

		if (length >= 4 && bytes.length >= index + 2) {
			int digit = bytes[++index];
			postfix *= _10;
			postfix += digit;

			digit = bytes[++index];
			postfix *= _10;
			postfix += digit;
			prefix *= 100;
		}

		final long id = prefix + postfix;
		if (log.isTraceEnabled()) {
			log.trace("{} = {}", pidId, id);
		}
		return id;
	}
}
