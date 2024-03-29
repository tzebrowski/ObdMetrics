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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.command.Command;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class StreamConnector implements Connector {

	private static final char NEXT_MESSAGE_SIGNAL = '>';
	private static final ConnectorResponse EMPTY_MESSAGE = ConnectorResponseFactory.wrap(new byte[] {}, 0, 0);

	@Getter
	private boolean faulty;

	@NonNull
	private OutputStream out;

	@NonNull
	private InputStream in;

	@NonNull
	private final AdapterConnection connection;
	private final Adjustments adjustments;

	private final byte[] buffer = new byte[BUFFER_SIZE];
	private long tts = 0;
	private boolean closed = false;

	StreamConnector(final AdapterConnection connection, final Adjustments adjustments) throws IOException {
		this.connection = connection;
		this.adjustments = adjustments;
		this.out = connection.openOutputStream();
		this.in = connection.openInputStream();
		reset();
	}

	@Override
	public void close() {
		log.info("Closing streams.");
		closed = true;
		faulty = false;
		try {
			if (out != null) {
				out.close();
				out = null;
			}
		} catch (final IOException e) {
		}
		try {
			if (in != null) {
				in.close();
				in = null;
			}
		} catch (final IOException e) {
		}

		try {
			connection.close();
		} catch (final IOException e) {
		}

	}

	@Override
	public synchronized void transmit(@NonNull final Command command) {
		tts = System.currentTimeMillis();
		if (isFaulty()) {
			log.warn("Previous IO failed. Cannot perform another IO operation");
		} else {
			try {
				if (adjustments != null && adjustments.isDebugEnabled()) {
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
						if (Characters.isCharacterAllowed(characterRead)) {
							buffer[cnt++] = (byte) Character.toUpperCase(characterRead);
						}
					}

					short start = 0;
					if ((char) buffer[0] == 'S' && (char) buffer[1] == 'E' && (char) buffer[2] == 'A'
							&& (char) buffer[3] == 'R') {
						// SEARCHING...
						start = 12;
						cnt = (short) (cnt - start);
					}

					final ConnectorResponse response = ConnectorResponseFactory.wrap(buffer, start, start + cnt);

					reset();

					tts = System.currentTimeMillis() - tts;
					if (adjustments != null && adjustments.isDebugEnabled()) {
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

	void reconnect() {
		if (closed) {
			log.error("Connection is closed. Do not try to reconnect.");
		} else {
			log.error("Connection is broken. Reconnecting...");
			try {
				connection.reconnect();
				in = connection.openInputStream();
				out = connection.openOutputStream();
				faulty = false;
			} catch (final IOException e) {
				faulty = true;
			}
		}
	}

	private void reset() {
		Arrays.fill(buffer, 0, buffer.length, (byte) 0);
	}
}