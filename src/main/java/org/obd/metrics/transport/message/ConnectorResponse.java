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

public interface ConnectorResponse extends Bytes {

	int NEGATIVE_CHARACTER = 56;
	int[] DEFAULT_COLON_POSTIONS = new int[] { -1, -1, -1, -1, -1, -1 };
	int TOKEN_LENGTH = 2;
	int TWO_TOKENS_LENGTH = 2 * TOKEN_LENGTH;
	byte COLON = 58;
	byte[] COLON_ARR = new byte[] { COLON };
	int RADIX = 16;

	default boolean isValueNegative(final PidDefinition pid) {
		return (char) at(pid.getSuccessCode().length()) >= NEGATIVE_CHARACTER;
	}

	default void processPositiveValue(final PidDefinition pid, final Numbers callback) {
		for (int pos = pid.getSuccessCode().length(), j = 0; pos < remaining(); pos += TOKEN_LENGTH, j++) {
			callback.processUnsigned(j, getUnsignedBy(pos));
		}
	}

	default void processAsSinglePositiveValue(final PidDefinition pid, final Numbers callback) {
		callback.processSingle(getAsSingleSignedValue(pid.getLength(), pid.getSuccessCode().length(), remaining()));
	}

	default void processNegativeValue(final PidDefinition pid, final Numbers callback) {
		callback.processSigned(getSignedBy(pid.getLength(), pid.getSuccessCode().length(), remaining()));
	}

	default int[] getColonPositions() {
		return DEFAULT_COLON_POSTIONS;
	}

	default String getRawValue(final PidDefinition pid) {
		return getMessage().subSequence(pid.getSuccessCode().length(), remaining()).toString();
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
