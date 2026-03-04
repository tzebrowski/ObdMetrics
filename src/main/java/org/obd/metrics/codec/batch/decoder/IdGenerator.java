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
package org.obd.metrics.codec.batch.decoder;

import org.obd.metrics.transport.message.ConnectorResponse;

final class IdGenerator {

	static long generate(final int length, final long pidId, int pos, final ConnectorResponse buffer) {
		long hash = pidId << 32;
		final int remaining = buffer.remaining();

		// Fast exit for empty reads or out of bounds
		if (length == 0 || pos + 1 >= remaining) {
			return hash;
		}

		// =========================================
		// TOKEN 1 (Handles length >= 1)
		// =========================================
		byte nextByte = buffer.at(pos + 1);
		if (nextByte == ConnectorResponse.COLON) {
			pos += ConnectorResponse.TOKEN_LENGTH;
			if (pos + 1 >= remaining) return hash;
			nextByte = buffer.at(pos + 1);
		}
		hash = (hash << 8) | (buffer.at(pos) & 0xFF);
		hash = (hash << 8) | (nextByte & 0xFF);
		pos += ConnectorResponse.TOKEN_LENGTH;
		
		if (length == 1 || pos + 1 >= remaining) return hash;

		// =========================================
		// TOKEN 2 (Handles length >= 2)
		// =========================================
		nextByte = buffer.at(pos + 1);
		if (nextByte == ConnectorResponse.COLON) {
			pos += ConnectorResponse.TOKEN_LENGTH;
			if (pos + 1 >= remaining) return hash;
			nextByte = buffer.at(pos + 1);
		}
		hash = (hash << 8) | (buffer.at(pos) & 0xFF);
		hash = (hash << 8) | (nextByte & 0xFF);
		pos += ConnectorResponse.TOKEN_LENGTH;
		
		if (length == 2 || pos + 1 >= remaining) return hash;

		// =========================================
		// TOKEN 3 (Handles length >= 3)
		// =========================================
		nextByte = buffer.at(pos + 1);
		if (nextByte == ConnectorResponse.COLON) {
			pos += ConnectorResponse.TOKEN_LENGTH;
			if (pos + 1 >= remaining) return hash;
			nextByte = buffer.at(pos + 1);
		}
		hash = (hash << 8) | (buffer.at(pos) & 0xFF);
		hash = (hash << 8) | (nextByte & 0xFF);
		pos += ConnectorResponse.TOKEN_LENGTH;
		
		if (length == 3 || pos + 1 >= remaining) return hash;

		// =========================================
		// TOKEN 4 (Handles length >= 4)
		// =========================================
		nextByte = buffer.at(pos + 1);
		if (nextByte == ConnectorResponse.COLON) {
			pos += ConnectorResponse.TOKEN_LENGTH;
			if (pos + 1 >= remaining) return hash;
			nextByte = buffer.at(pos + 1);
		}
		hash = (hash << 8) | (buffer.at(pos) & 0xFF);
		hash = (hash << 8) | (nextByte & 0xFF);
		pos += ConnectorResponse.TOKEN_LENGTH;
		
		if (length == 4 || pos + 1 >= remaining) return hash;

		// =========================================
		// FALLBACK LOOP (Handles length 5+)
		// =========================================
		int tokensRead = 4;
		while (tokensRead < length && pos + 1 < remaining) {
			nextByte = buffer.at(pos + 1);
			if (nextByte == ConnectorResponse.COLON) {
				pos += ConnectorResponse.TOKEN_LENGTH;
				if (pos + 1 >= remaining) break;
				nextByte = buffer.at(pos + 1);
			}
			
			hash = (hash << 8) | (buffer.at(pos) & 0xFF);
			hash = (hash << 8) | (nextByte & 0xFF);
			
			pos += ConnectorResponse.TOKEN_LENGTH;
			tokensRead++;
		}

		return hash;
	}
}