 /**
 * Copyright 2019-2025, Tomasz Żebrowski
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
package org.obd.metrics.transport;

import java.io.IOException;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.command.Command;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class SniffingConnector extends AbstractConnector {
	private static final byte[] PATTERN_1 = "BUFFER".getBytes();
	private static final byte[] PATTERN_2 = "STOPPED".getBytes();

	SniffingConnector(final AdapterConnection connection, final Adjustments adjustments) throws IOException {
		super(BufferSize.DEFAULT * 10, connection, adjustments);
	}

	@Override
	public synchronized void transmit(@NonNull final Command command) {
		tts = System.currentTimeMillis();
		if (isFaulty()) {
			log.warn("Previous IO failed. Cannot perform another IO operation");
		} else {
			try {
				if (adjustments != null && adjustments.getSniffing().isDebugEnabled()) {
					log.info("TX: {}", command.getQuery());
				}
				if (out != null) {
					out.write(command.getData());
				}
			} catch (final IOException e) {
				log.error("Failed to transmit command: {}", command, e);
				reconnect();
			}
		}
	}

	@Override
	public synchronized ConnectorResponse receive() {
		if (isFaulty()) {
			log.warn("Previous IO failed. Cannot perform another IO operation");
		} else {
			try {
				if (in != null) {
					short cnt = 0;
					int nextByte;
					char characterRead;

					while ((nextByte = in.read()) > -1 && (characterRead = (char) nextByte) != NEXT_MESSAGE_SIGNAL
							&& cnt != buffer.length) {
						buffer[cnt++] = (byte) Character.toUpperCase(characterRead);
					}

					short start = 0;
					if ((char) buffer[0] == 'A' && (char) buffer[1] == 'T' && (char) buffer[2] == 'M'
							&& (char) buffer[3] == 'A') {
						start = 4;
					} else if ((char) buffer[0] == 'S' && (char) buffer[1] == 'T' && (char) buffer[2] == 'M'
							&& (char) buffer[3] == 'A') {
						start = 4;
					} else if ((char) buffer[0] == 'S' && (char) buffer[1] == 'T' && (char) buffer[2] == 'M') {
						start = 3;
					}

					cnt = (short) (cnt - start);
					short patternIndex = indexOf(buffer, PATTERN_1, (short)(cnt/2));
					if (patternIndex > 0) {
						cnt = patternIndex;
					} else {
						patternIndex = indexOf(buffer, PATTERN_2, (short)(cnt/2));
						if (patternIndex > 0) {
							cnt = patternIndex;
						} else {
							cnt = (short) (start + cnt);
						}
					}
					
					if (cnt-2 > 0 ) {
						cnt-=2;
					}
					
					final ConnectorResponse response = connectorResponsefactory.wrap(buffer, start, cnt);
					reset();

					tts = System.currentTimeMillis() - tts;
					if (adjustments != null && adjustments.getSniffing().isDebugEnabled()) {
						log.info("RX: {}, processing time: {}ms", response.getMessage(), tts);
					}

					return response;
				}
			} catch (final IOException e) {
				log.error("Failed to receive data", e);
				reconnect();
			}
		}
		return EMPTY_MESSAGE;
	}

	private short indexOf(final byte[] buffer, final byte[] str, final short fromIndex) {
		final int strCount = str.length;
		
		final int valueCount = buffer.length;
		final byte first = str[0];
		final int max = (valueCount - strCount);
		for (short i = fromIndex; i <= max; i++) {
			if (buffer[i] != first) {
				while (++i <= max && buffer[i] != first) {
					;
				}
			}
			if (i <= max) {
				int j = i + 1;
				final int end = j + strCount - 1;
				for (int k = 1; j < end && buffer[j] == str[k]; j++, k++) {
					;
				}
				if (j == end) {
					return i;
				}
			}
		}
		return -1;
	}
}