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

import org.obd.metrics.pid.CommandType;
import org.obd.metrics.pid.PidDefinition;

public interface ConnectorResponse {
	
	int NEGATIVE_CHARACTER = 56;
	int[] DEFAULT_COLON_POSTIONS = new int[] { -1, -1, -1, -1, -1, -1 };
	int TOKEN_LENGTH = 2;
	int TWO_TOKENS_LENGTH = 2 * TOKEN_LENGTH;
	byte COLON = 58;
	byte[] COLON_ARR = new byte[] { COLON };
	int RADIX = 16;
	
	byte byteAt(int index);

	int remaining();

	long capacity();

	default boolean isNegative(final PidDefinition pid) {
		return (char)byteAt(pid.getSuccessCode().length()) >= NEGATIVE_CHARACTER;
	}
	
	default int toDecimal(final int pos) {
		
		int result = 0;
		int i = pos;
		int digit = Character.digit(byteAt(i++) & 0xFF, RADIX);
		result -= digit;

		digit = Character.digit(byteAt(i++) & 0xFF, RADIX);
		result *= RADIX;
		result -= digit;

		return -result;
	}
	
	default short toDecimal(final PidDefinition pid, int start, int end) throws NumberFormatException {

		boolean negative = false;
		int len = end;
		int limit = -Integer.MAX_VALUE;

		if (len > 0) {
			
			int multmin = limit / RADIX;
			int result = 0;
			while (start < len) {
				final int digit = Character.digit(byteAt(start++), RADIX);
				if (digit < 0 || result < multmin) {
					throw new NumberFormatException("Invalid digit");
				}
				result *= RADIX;
				if (result < limit + digit) {
					throw new NumberFormatException("Invalid digit");
				}
				result -= digit;
			}
			return (short)(negative ? result : -result);
		} else {
			throw new NumberFormatException("Invalid digit");
		}
	}
	
	default int[] getColonPositions() {
		return DEFAULT_COLON_POSTIONS;
	}

	default void exctractDecimals(final PidDefinition pid, final DecimalReceiver decimalHandler) {
		for (int pos = pid.getSuccessCode().length(), j = 0; pos < remaining(); pos += TOKEN_LENGTH, j++) {
			decimalHandler.receive(j, toDecimal(pos));
		}
	}
	
	default void exctractSingleDecimal(final PidDefinition pid, final DecimalReceiver decimalHandler) {
		decimalHandler.receive(toDecimal(pid, pid.getSuccessCode().length(),remaining()));
	}
	
	default String getRawValue(final PidDefinition pid) {
		return getMessage().subSequence(pid.getSuccessCode().length(),remaining()).toString();
	}

	default int indexOf(final byte[] str, final int strCount, final int fromIndex) {

		final int valueCount = remaining();
		final byte first = str[0];
		final int max = (valueCount - strCount);
		for (int i = fromIndex; i <= max; i++) {
			if (byteAt(i) != first) {
				while (++i <= max && byteAt(i) != first) {
					;
				}
			}
			if (i <= max) {
				int j = i + 1;
				final int end = j + strCount - 1;
				for (int k = 1; j < end && byteAt(j) == str[k]; j++, k++) {
					;
				}
				if (j == end) {
					return i;
				}
			}
		}
		return -1;
	}

	default boolean isResponseCodeSuccess(PidDefinition pidDefinition) {
		if (CommandType.OBD.equals(pidDefinition.getCommandType())) {
			// success code = 0x40 + mode + pid
			return isReponseCodeSuccess(pidDefinition.getSuccessCodeBytes());
		} else {
			return true;
		}
	}

	default boolean isReponseCodeSuccess(final byte[] expectedAnswer) {
		return true;
	}

	default boolean isCacheable() {
		return false;
	}

	default long id() {
		return -1L;
	}

	default String getMessage() {
		return null;
	}


	default boolean isEmpty() {
		return false;
	}

	default AdapterErrorType findError() {
		return findError(false);
	}

	default AdapterErrorType findError(boolean fullSearch) {
		return AdapterErrorType.NONE;
	}
}
