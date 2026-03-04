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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

final class RawConnectorResponse implements ConnectorResponse {

	// Pre-allocated array tied to the lifecycle of this specific object.
	// Completely prevents the global memory corruption bug.
	private final int[] colonsArray = new int[16];
	private boolean colonsCalculated = false;

	private final byte[] bytes;

	private String message;

	private int remaining;

	RawConnectorResponse(Integer capacity) {
		bytes = new byte[capacity];
		remaining = bytes.length;
		reset();
	}

	@Override
	public int[] getColonPositions() {
		if (!colonsCalculated) {
			Arrays.fill(colonsArray, -1);
			int fromIndex = 0;

			for (int i = 0; i < colonsArray.length; i++) {
				int colonIndex = indexOf(COLON_ARR, 1, fromIndex);
				if (colonIndex > -1) {
					colonsArray[i] = colonIndex;
					fromIndex = colonIndex + 1;
				} else {
					break;
				}
			}
			colonsCalculated = true;
		}
		return colonsArray;
	}

	@Override
	public long capacity() {
		return bytes.length;
	}

	@Override
	public String getMessage() {
		if (message == null && bytes != null) {
			message = new String(Arrays.copyOf(bytes, remaining), StandardCharsets.ISO_8859_1);
		}
		return message;
	}

	@Override
	public int remaining() {
		return remaining;
	}

	@Override
	public boolean isReponseCodeSuccess(final byte[] expected) {
		if (expected.length == 4) {
			return expected[0] == bytes[0] && expected[1] == bytes[1] && expected[2] == bytes[2]
					&& expected[3] == bytes[3];

		} else if (expected.length == 6) {
			return expected[0] == bytes[0] && expected[1] == bytes[1] && expected[2] == bytes[2]
					&& expected[3] == bytes[3] && expected[4] == bytes[4] && expected[5] == bytes[5];
		} else {
			return Arrays.equals(expected, 0, expected.length, bytes, 0, expected.length);
		}
	}

	@Override
	public boolean isEmpty() {
		return bytes == null || remaining == 0
				|| (remaining >= 4 && bytes[0] == 'N' && bytes[1] == 'O' && bytes[2] == 'D' && bytes[3] == 'A');
	}

	@Override
	public AdapterErrorType findError(boolean longPath) {
		if (bytes == null || remaining == 0) {
			return AdapterErrorType.NO_DATA;
		} else {
			if (longPath) {
				for (final AdapterErrorType error : AdapterErrorType.values()) {
					if (indexOf(error.getBytes(), error.getBytes().length, 0) > 0) {
						return error;
					}
				}
			} else {
				if (remaining >= 3) {
					switch (bytes[0]) {
					case 'S':
						if (remaining >= 4 && bytes[1] == 'T' && bytes[2] == 'O' && bytes[3] == 'P')
							return AdapterErrorType.STOPPED;
						break;
					case 'E':
						if (remaining >= 4 && bytes[1] == 'R' && bytes[2] == 'R' && bytes[3] == 'O')
							return AdapterErrorType.ERROR;
						break;
					case 'C':
						if (remaining >= 4 && bytes[1] == 'A' && bytes[2] == 'N' && bytes[3] == 'E')
							return AdapterErrorType.CANERROR;
						break;
					case 'B':
						if (remaining >= 4 && bytes[1] == 'U' && bytes[2] == 'S' && bytes[3] == 'I')
							return AdapterErrorType.BUSINIT;
						break;
					case 'U':
						if (remaining >= 4 && bytes[1] == 'N' && bytes[2] == 'A' && bytes[3] == 'B')
							return AdapterErrorType.UNABLETOCONNECT;
						break;
					case 'L':
						if (remaining >= 5 && bytes[1] == 'V' && bytes[2] == 'R' && bytes[3] == 'E' && bytes[4] == 'S')
							return AdapterErrorType.LVRESET;
						break;
					case 'F':
						if (remaining >= 10 && bytes[1] == 'C' && bytes[2] == 'R' && bytes[3] == 'X' && bytes[4] == 'T'
								&& bytes[5] == 'I' && bytes[6] == 'M' && bytes[7] == 'E' && bytes[8] == 'O'
								&& bytes[9] == 'U')
							return AdapterErrorType.FCRXTIMEOUT;
						break;
					}
				}
			}
		}
		return AdapterErrorType.NONE;
	}

	void update(byte[] in, int from, int to) {
		reset();
		System.arraycopy(in, from, bytes, 0, to);
		remaining = to - from;
	}

	private void reset() {
		Arrays.fill(bytes, 0, bytes.length, (byte) 0);
		message = null;
		colonsCalculated = false;
	}

	@Override
	public byte at(int index) {
		if (index >= remaining) {
			return -1;
		} else {
			return bytes[index];
		}
	}
}