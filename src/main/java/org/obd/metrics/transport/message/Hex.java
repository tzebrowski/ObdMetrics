/** 
 * Copyright 2019-2024, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.transport.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Hex {
	private static final int RADIX = 16;
	
	public static String decode(String message) {
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
	
	public static int getUnsignedNumberBy(final Bytes bytes, final int pos) {

		int result = 0;
		int i = pos;
		int digit = Character.digit(bytes.at(i++) & 0xFF, RADIX);
		result -= digit;

		digit = Character.digit(bytes.at(i++) & 0xFF, RADIX);
		result *= RADIX;
		result -= digit;

		return -result;
	}

	public static int getSignedNumberBy(final Bytes bytes, int length, int start, int end) throws NumberFormatException {

		boolean negative = false;
		int len = end;
		int limit = -Integer.MAX_VALUE;

		if (len > 0) {

			int multmin = limit / RADIX;
			int result = 0;
			while (start < len) {
				final int digit = Character.digit(bytes.at(start++), RADIX);
				if (digit < 0 || result < multmin) {
					throw new NumberFormatException("Invalid digit");
				}
				result *= RADIX;
				if (result < limit + digit) {
					throw new NumberFormatException("Invalid digit");
				}
				result -= digit;
			}
			int val = (negative ? result : -result);
			return length == 1 ? ((val + 0x80) & 0xFF) - 0x80 : ((val + 0x8000) & 0xFFFF) - 0x8000;
		} else {
			throw new NumberFormatException("Invalid digit");
		}
	}
}
