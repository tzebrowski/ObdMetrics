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
package org.obd.metrics.transport;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CanUtils {

	private static final int CAN_SFF_MASK = 0x000007FF;
	private static final int CAN_EFF_MASK = 0x1FFFFFFF;

	public static String intToHex(int value) {
		return Integer.toHexString(value).toUpperCase();
	}
	
	public static int hexToInt(String hex) {
		final byte[] bytes = new BigInteger(hex, 16).toByteArray();
		final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.put(bytes);
		buffer.rewind();
		return buffer.getInt();
	}

	public static String canIdToHex(final byte[] canIdArray) {

		final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.put(canIdArray);
		buffer.rewind();
		final int canId = buffer.getInt();

		if ((canId & CAN_EFF_MASK) == 1) {
			return Integer.toHexString(canId & CAN_EFF_MASK);
		} else {
			return Integer.toHexString(canId & CAN_SFF_MASK);
		}
	}
}
