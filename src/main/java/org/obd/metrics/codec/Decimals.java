package org.obd.metrics.codec;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class Decimals {

	int toDecimal(byte[] bytes, int index, int length) {
		final char[] value = toChars(bytes, index, length);
		int radix = 16;
		int i = 0, len = value.length;
		int limit = -Integer.MAX_VALUE;

		boolean negative = false;
		char firstChar = value[0];
		if (firstChar < '0') {
			if (firstChar == '-') {
				negative = true;
				limit = Integer.MIN_VALUE;
			}
			i++;
		}

		int result = 0;
		while (i < len) {
			int digit = Character.digit(value[i++], radix);
			result *= radix;
			if (result < limit + digit) {
				return 0;
			}
			result -= digit;
		}
		return negative ? result : -result;
	}

	private void inflate(byte[] src, int srcOff, char[] dst, int dstOff, int len) {
		for (int i = 0; i < len; i++) {
			dst[dstOff++] = (char) (src[srcOff++] & 0xff);
		}
	}

	private char[] toChars(byte[] value, int index, int len) {
		final char[] dst = new char[len];
		inflate(value, index, dst, 0, len);
		return dst;
	}
}
