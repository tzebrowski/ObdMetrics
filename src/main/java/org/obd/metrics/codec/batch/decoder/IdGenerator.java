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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class IdGenerator {

	/**
	 * Generates a unique 64-bit ID based on the PID ID and its raw payload bytes.
	 * Actively skips multi-frame sequence colons (e.g. "1:") to ensure consistent caching.
	 * Utilizes bitwise operations for enhanced performance.
	 */
	static long generate(final int length, final long pidId, int pos, final ConnectorResponse buffer) {
		// Start with the PID. Shift left by 32 bits to reserve space for payload bytes.
		long hash = pidId << 32;
		int tokensToRead = length;
		int tokensRead = 0;
		
		while (tokensRead < tokensToRead && pos + 1 < buffer.remaining()) {
			
			// Skip multi-frame sequence delimiter (e.g. '1:')
			if (buffer.at(pos + 1) == ConnectorResponse.COLON) {
				pos += ConnectorResponse.TOKEN_LENGTH;
				
				// Ensure we haven't skipped past the end of the buffer
				if (pos + 1 >= buffer.remaining()) {
					break;
				}
			}
			
			// Shift the hash left by 8 bits and incorporate the raw ASCII byte
			hash = (hash << 8) | (buffer.at(pos) & 0xFF);
			hash = (hash << 8) | (buffer.at(pos + 1) & 0xFF);
			
			pos += ConnectorResponse.TOKEN_LENGTH;
			tokensRead++;
		}
		
		if (log.isTraceEnabled()) {
			log.trace("{} = {}", pidId, hash);
		}
	
		return hash;
	}
}