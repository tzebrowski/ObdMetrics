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
package org.obd.metrics.test.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.transport.AdapterConnection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MockAdapterConnection implements AdapterConnection {

	@AllArgsConstructor
	static final class Out extends ByteArrayOutputStream {
		final Map<String, String> requestResponse;
		final MutableByteArrayInputStream in;
		final long writeTimeout;
		final boolean simulateWriteError;
		@Getter
		private final LinkedBlockingDeque<String> recordedQueries = new LinkedBlockingDeque<>();
		
		@Override
		public void write(byte[] buff) throws IOException {
			if (simulateWriteError) {
				throw new IOException("Write exception");
			}
			if (buff == null || buff.length == 0) {
				//
			} else {
				final String command = new String(buff).trim().replaceAll("\r", "");
				log.trace("In command: {}", command);
				recordedQueries.addLast(command);

				try {
					TimeUnit.MILLISECONDS.sleep(writeTimeout);
				} catch (InterruptedException e) {
				}

				if (requestResponse.containsKey(command)) {
					final String answer = requestResponse.get(command);
					log.trace("Matches: {} = {}", command, answer);
					in.update(answer);
				}
			}
		}
	}

	private Out output;
	private MutableByteArrayInputStream input;
	private boolean simulateErrorInReconnect = false;

	public BlockingDeque<String> recordedQueries() {
		return output.recordedQueries;
	}

	@Builder
	public static MockAdapterConnection build(@Singular("requestResponse") Map<String, String> requestResponse,
	        long writeTimeout,
	        long readTimeout, boolean simulateWriteError, boolean simulateReadError, boolean simulateErrorInReconnect) {

		final MockAdapterConnection connection = new MockAdapterConnection();
		connection.simulateErrorInReconnect = simulateErrorInReconnect;
		connection.input = new MutableByteArrayInputStream(readTimeout, simulateReadError);
		connection.output = new Out(wrap(requestResponse), connection.input, writeTimeout, simulateWriteError);
		return connection;
	}

	private static Map<String, String> wrap(Map<String, String> parameters) {
		final Map<String, String> genericAnswers = new HashMap<>();
		genericAnswers.putAll(GenericAnswers.genericAnswers());
		genericAnswers.putAll(parameters);// override
		return genericAnswers;
	}

	@Override
	public void connect() throws IOException {

	}

	@Override
	public InputStream openInputStream() throws IOException {
		return input;
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return output;
	}

	@Override
	public void reconnect() throws IOException {
		if (simulateErrorInReconnect) {
			throw new IOException("Reconnect exception");
		}
	}

	@Override
	public void close() throws IOException {
	}
}
