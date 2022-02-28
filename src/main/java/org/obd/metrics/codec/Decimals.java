package org.obd.metrics.codec;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class Decimals {
	static final int RADIX = 16;
	
	int twoBytesToDecimal(byte[] bytes, int pos) {
		
		int result = 0;
		int i = pos;
		int digit = Character.digit(bytes[i++] & 0xFF, RADIX);
		result -= digit;
		
		digit = Character.digit(bytes[i++] & 0xFF, RADIX);
		result *= RADIX;
		result -= digit;
		
		return -result;
	}
}
