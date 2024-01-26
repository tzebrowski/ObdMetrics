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
import org.obd.metrics.command.obd.CannelloniMessage;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CannelloniConnector implements Connector {

	private static final char NEXT_MESSAGE_SIGNAL = '\n';
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

	CannelloniConnector(final AdapterConnection connection, final Adjustments adjustments) throws IOException {
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
					if ( command instanceof CannelloniMessage) {
						log.info("TX: {}", printMessage((CannelloniMessage) command));
					} else {
						log.info("TX: {}", command.getQuery());
					}
				}
				out.write(command.getData());
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
						// CANNELLONIv1
						if (buffer[0] == 'C' && buffer[1] == 'A' && buffer[2] == 'N' && buffer[3] == 'N'
								&& buffer[4] == 'E' && buffer[5] == 'L' && buffer[6] == 'L' && buffer[7] == 'O'
								&& buffer[8] == 'N' && buffer[9] == 'I' && buffer[10] == 'V' && buffer[11] == '1') {
							break;
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

	private String printMessage(CannelloniMessage message) {
		final StringBuilder buffer = new StringBuilder();
		buffer.append("[");
		buffer.append(CanUtils.canIdToHex(new byte[] {
				message.getData()[0], 
				message.getData()[1], 
				message.getData()[2], 
				message.getData()[3]}));
		buffer.append("]");
		
		buffer.append(" ");
		
		for (int i = 5; i< message.getData().length; i++) {
			final byte b = message.getData()[i];
			buffer.append(String.format("%02X ", b));
		}
		return buffer.toString();
	}
}