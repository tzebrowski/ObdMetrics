/** 
 * Copyright 2019-2023, Tomasz Żebrowski
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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.obd.metrics.transport.Connector;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "message")
final class RawConnectorResponse implements ConnectorResponse {
	
	private int colonsArray[] = null;
	
	private final byte[] bytes;
	
	private String message;

	private int remaining;
	
	RawConnectorResponse() {
		this(Connector.BUFFER_SIZE);
	}

	RawConnectorResponse(int capacity) {
		bytes = new byte[capacity];
		remaining = bytes.length;
		reset();
	}
	
	@Override
	public int[] getColonPositions() {
		if (colonsArray == null) {
			int fromIndex = 0;
			colonsArray = new int[] { -1, -1, -1, -1, -1, -1 };

			for (int i = 0; i < colonsArray.length; i++) {
				int colonIndex = indexOf(COLON_ARR, 1, fromIndex);
				if (colonIndex > -1) {
					fromIndex = colonIndex + 1;
				}
				colonsArray[i] = colonIndex;
			}
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
	public boolean isTimeout() {
		return bytes == null || remaining == 0
				|| ((bytes[0] == 'F') && (bytes[1] == 'C') && (bytes[2] == 'R') && (bytes[3] == 'X') && 
					(bytes[4] == 'T') && (bytes[5] == 'I') && (bytes[6] == 'M') && (bytes[7] == 'E') && 
					(bytes[8] == 'O') && (bytes[9] == 'U') && (bytes[10] == 'T'));
	}
	
	@Override
	public boolean isEmpty() {
		return bytes == null || remaining == 0
				|| ((bytes[0] == 'N') && (bytes[1] == 'O') && (bytes[2] == 'D') && (bytes[3] == 'A'));
	}
	//LVRESET
	
	@Override
	public boolean isLowVoltageReset() {
		return bytes == null || remaining == 0
				|| ((bytes[0] == 'L') && (bytes[1] == 'V') && (bytes[2] == 'R') && (bytes[3] == 'E') 
						&& (bytes[4] == 'S'));
	}
	
	@Override
	public boolean isError() {
		return bytes == null || remaining == 0
				|| (remaining >= 3 && (bytes[0] == 'S') && (bytes[1] == 'T') && (bytes[2] == 'O')
						&& (bytes[3] == 'P'))
				|| (remaining >= 3 && (bytes[0] == 'E') && (bytes[1] == 'R') && (bytes[2] == 'R')
						&& (bytes[3] == 'O'))
				|| (remaining >= 3 && (bytes[0] == 'U') && (bytes[1] == 'N') && (bytes[2] == 'A')
						&& (bytes[3] == 'B'))
				|| (remaining >= 3 && (bytes[0] == 'B') && (bytes[1] == 'U') && (bytes[2] == 'S')
						&& (bytes[3] == 'I'))
				|| (remaining >= 3 && (bytes[0] == 'C') && (bytes[1] == 'A') && (bytes[2] == 'N')
						&& (bytes[3] == 'E'));
	}
	
	void update(byte[] in, int from, int to) {
		reset();
		System.arraycopy(in, from, bytes, 0, to);
		remaining = to - from;
	}
	
	private void reset() {
		Arrays.fill(bytes, 0, bytes.length, (byte) 0);
		message = null;
	}

	@Override
	public byte byteAt(int index) {
		return bytes[index];
	}
}
