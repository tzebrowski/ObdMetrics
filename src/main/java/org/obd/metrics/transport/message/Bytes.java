 /**
 * Copyright 2019-2026, Tomasz Żebrowski
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.obd.metrics.transport.message;

interface Bytes {
	int RADIX = 16;

	byte at(int index);

	int remaining();

	long capacity();

	default int indexOf(final byte[] str, final int strCount, final int fromIndex) {

		final int valueCount = remaining();
		final byte first = str[0];
		final int max = (valueCount - strCount);
		for (int i = fromIndex; i <= max; i++) {
			if (at(i) != first) {
				while (++i <= max && at(i) != first) {
					;
				}
			}
			if (i <= max) {
				int j = i + 1;
				final int end = j + strCount - 1;
				for (int k = 1; j < end && at(j) == str[k]; j++, k++) {
					;
				}
				if (j == end) {
					return i;
				}
			}
		}
		return -1;
	}

	default int getUnsignedBy(final int pos) {

		int result = 0;
		int i = pos;
		int digit = Character.digit(at(i++) & 0xFF, RADIX);
		result -= digit;

		digit = Character.digit(at(i++) & 0xFF, RADIX);
		result *= RADIX;
		result -= digit;

		return -result;
	}

	default int getSignedBy(int length, int start, int end) {
		final int val = getSingleSignedValue(length, start, end);
		return length == 1 ? ((val + 0x80) & 0xFF) - 0x80 : ((val + 0x8000) & 0xFFFF) - 0x8000;
	}

	default int getSingleSignedValue(int length, int start, int end) {
		if (end > 0) {
			int colonIdx = -1;
			int startIdx = start;
			if (length < end - startIdx) {
				while (startIdx < end) {
					if (at(startIdx) == ConnectorResponse.COLON) {
						colonIdx = startIdx - 1;
						break;
					}
					startIdx++;
				}
			}

			int multmin = -Integer.MAX_VALUE / RADIX;
			int result = 0;

			boolean negative = false;
			while (start < end) {
				if (colonIdx > 0 && start == colonIdx) {
					start += ConnectorResponse.TOKEN_LENGTH;
				}

				if (start >= end)
					break;

				int digit = Character.digit(at(start++), RADIX);

				// Robustly skip any non-hex noise characters (like multiple spaces or \r)
				while (digit < 0 && start < end) {
					digit = Character.digit(at(start++), RADIX);
				}

				// If we exhausted the string and found only trailing noise, we can safely break
				if (digit < 0) {
					break;
				}

				if (result < multmin) {
					throw new NumberFormatException(String.format("Invalid digit[%d] length[%d], start[%d], end[%d]",
							digit, length, start, end));
				}

				result *= RADIX;
				if (result < -Integer.MAX_VALUE + digit) {
					throw new NumberFormatException("Invalid digit - integer overflow");
				}
				result -= digit;
			}

			return (negative ? result : -result);
		} else {
			throw new NumberFormatException("Invalid bounds");
		}
	}
}