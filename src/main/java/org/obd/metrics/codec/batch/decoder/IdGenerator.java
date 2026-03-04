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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class IdGenerator {

	/**
	 * Generates a unique 64-bit ID based on the PID ID and its raw payload bytes.
	 * Micro-optimized for high-throughput (ops/ms) execution.
	 */
	static long generate(final int length, final long pidId, int pos, final ConnectorResponse buffer) {
		long hash = pidId << 32;
		
		int tokensRead = 0;
		// 1. Cache the boundary limits OUTSIDE the loop.
		final int remaining = buffer.remaining(); 
		
		while (tokensRead < length && pos + 1 < remaining) {
			
			byte nextByte = buffer.at(pos + 1);
			
			if (nextByte == ConnectorResponse.COLON) {
				pos += ConnectorResponse.TOKEN_LENGTH;
				
				if (pos + 1 >= remaining) {
					break;
				}
				// Re-read the next byte since we shifted position
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