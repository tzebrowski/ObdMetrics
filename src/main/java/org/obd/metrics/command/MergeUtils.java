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
package org.obd.metrics.command;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MergeUtils {

	static byte[] merge(final String canIdHex, final String dataHex) {
		final byte[] data = hexStringToByteArray(dataHex.replace(" ", ""));
		final byte[] canId = hexStringToByteArray(canIdHex);
		final byte[] header = new byte[] { 0x0, 0x0, 0x0, 0x0 };
		
		for (int i = 0; i < canId.length; i++) {
			final byte b = canId[i];
			header[i + header.length - canId.length] = b;
		}

		final ByteBuffer buff = ByteBuffer.wrap(new byte[header.length + 1 + data.length]);
		buff.put(header);
		buff.put((byte) data.length);
		buff.put(data);
		return buff.array();
	}

	static byte[] merge(final byte[] canId, final byte[] data) {
		final byte[] allByteArray = new byte[canId.length + 1 + data.length];
		final ByteBuffer buff = ByteBuffer.wrap(allByteArray);
		buff.put(canId);
		buff.put((byte) data.length);
		buff.put(data);
		return buff.array();
	}

	private static byte[] hexStringToByteArray(String hex) {
		return new BigInteger(hex, 16).toByteArray();
	}

}
