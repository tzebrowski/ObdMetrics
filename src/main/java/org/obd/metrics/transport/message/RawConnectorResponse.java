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
	public boolean isEmpty() {
		return bytes == null || remaining == 0
				|| ((bytes[0] == AdapterErrorType.NO_DATA.getBytes()[0]) 
						&& (bytes[1] == AdapterErrorType.NO_DATA.getBytes()[1]) 
						&& (bytes[2] == AdapterErrorType.NO_DATA.getBytes()[2]) 
						&& (bytes[3] == AdapterErrorType.NO_DATA.getBytes()[3]));
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
					if ((bytes[0] == AdapterErrorType.STOPPED.getBytes()[0]) 
							&& (bytes[1] == AdapterErrorType.STOPPED.getBytes()[1]) 
							&& (bytes[2] == AdapterErrorType.STOPPED.getBytes()[2])
							&& (bytes[3] == AdapterErrorType.STOPPED.getBytes()[3])) {
						return AdapterErrorType.STOPPED;
					} else if ((bytes[0] == AdapterErrorType.ERROR.getBytes()[0]) 
							&& (bytes[1] == AdapterErrorType.ERROR.getBytes()[1]) 
							&& (bytes[2] == AdapterErrorType.ERROR.getBytes()[2])
							&& (bytes[3] == AdapterErrorType.ERROR.getBytes()[3])) {
						return AdapterErrorType.ERROR;
					}else if ((bytes[0] == AdapterErrorType.CANERROR.getBytes()[0]) 
							&& (bytes[1] == AdapterErrorType.CANERROR.getBytes()[1]) 
							&& (bytes[2] == AdapterErrorType.CANERROR.getBytes()[2])
							&& (bytes[3] == AdapterErrorType.CANERROR.getBytes()[3])){
						return AdapterErrorType.CANERROR;
					}else if ((bytes[0] == AdapterErrorType.BUSINIT.getBytes()[0]) 
							&& (bytes[1] == AdapterErrorType.BUSINIT.getBytes()[1]) 
							&& (bytes[2] == AdapterErrorType.BUSINIT.getBytes()[2])
							&& (bytes[3] == AdapterErrorType.BUSINIT.getBytes()[3])){
						return AdapterErrorType.BUSINIT;
					}else if  ((bytes[0] == AdapterErrorType.UNABLETOCONNECT.getBytes()[0]) 
							&& (bytes[1] == AdapterErrorType.UNABLETOCONNECT.getBytes()[1]) 
							&& (bytes[2] == AdapterErrorType.UNABLETOCONNECT.getBytes()[2])
							&& (bytes[3] == AdapterErrorType.UNABLETOCONNECT.getBytes()[3])){
						return AdapterErrorType.UNABLETOCONNECT;
					} else if  ((bytes[0] == AdapterErrorType.LVRESET.getBytes()[0]) 
							&& (bytes[1] == AdapterErrorType.LVRESET.getBytes()[1]) 
							&& (bytes[2] == AdapterErrorType.LVRESET.getBytes()[2]) 
							&& (bytes[3] == AdapterErrorType.LVRESET.getBytes()[3]) 
							&& (bytes[4] == AdapterErrorType.LVRESET.getBytes()[4])) {
						return AdapterErrorType.LVRESET;
					} else if  ((bytes[0] == AdapterErrorType.FCRXTIMEOUT.getBytes()[0]) 
							&& (bytes[1] == AdapterErrorType.FCRXTIMEOUT.getBytes()[1]) 
							&& (bytes[2] == AdapterErrorType.FCRXTIMEOUT.getBytes()[2]) 
							&& (bytes[3] == AdapterErrorType.FCRXTIMEOUT.getBytes()[3])
							&& (bytes[4] == AdapterErrorType.FCRXTIMEOUT.getBytes()[4])
							&& (bytes[5] == AdapterErrorType.FCRXTIMEOUT.getBytes()[5])
							&& (bytes[6] == AdapterErrorType.FCRXTIMEOUT.getBytes()[6])
							&& (bytes[7] == AdapterErrorType.FCRXTIMEOUT.getBytes()[7])
							&& (bytes[8] == AdapterErrorType.FCRXTIMEOUT.getBytes()[8])
							&& (bytes[9] == AdapterErrorType.FCRXTIMEOUT.getBytes()[9])) {
						return AdapterErrorType.FCRXTIMEOUT;
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
		colonsArray = null;
	}

	@Override
	public byte byteAt(int index) {
		if (index >= remaining) {
			return -1;
		} else {
			return bytes[index];
		}
	}
}
