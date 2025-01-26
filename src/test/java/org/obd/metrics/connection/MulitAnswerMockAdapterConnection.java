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
package org.obd.metrics.connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.transport.AdapterConnection;

import com.google.common.collect.Iterables;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MulitAnswerMockAdapterConnection implements AdapterConnection {
	
	private final static Map<String, Iterator<String>> genericAnswers() {
		final Map<String, Iterator<String>> requestResponse = new HashMap<>();
		requestResponse.put("ATZ", List.of("connected?").iterator());
		requestResponse.put("ATL0",List.of("atzelm327v1.5").iterator());
		requestResponse.put("ATH0", List.of("ath0ok").iterator());
		requestResponse.put("ATE0", List.of("ate0ok").iterator());
		requestResponse.put("ATSP0", List.of("ok").iterator());
		requestResponse.put("AT I", List.of("elm327v1.5").iterator());
		requestResponse.put("AT @1", List.of("obdiitors232interpreter").iterator());
		requestResponse.put("AT @2", List.of("?").iterator());
		requestResponse.put("AT DP", List.of("auto").iterator());
		requestResponse.put("AT DPN", List.of("a0").iterator());
		requestResponse.put("AT RV", List.of("11.8v").iterator());
		return requestResponse;
	}
	
	
	@AllArgsConstructor
	static final class Out extends ByteArrayOutputStream {
		final Map<String, Iterator<String>> requestResponse;
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
					final Iterator<String> collection = requestResponse.get(command);
					log.trace("Matches: {} = {}", command, collection);
					if (collection.hasNext()) {
						in.update(collection.next());
					}
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
	public static MulitAnswerMockAdapterConnection build(@Singular("requestResponse") Map<String, List<String>> requestResponse,
	        long writeTimeout,
	        long readTimeout, boolean simulateWriteError, boolean simulateReadError, boolean simulateErrorInReconnect) {

		final MulitAnswerMockAdapterConnection connection = new MulitAnswerMockAdapterConnection();
		connection.simulateErrorInReconnect = simulateErrorInReconnect;
		connection.input = new MutableByteArrayInputStream(readTimeout, simulateReadError);
		connection.output = new Out(wrap(requestResponse), connection.input, writeTimeout, simulateWriteError);
		return connection;
	}

	private static Map<String, Iterator<String>> wrap(Map<String, List<String>> parameters) {
		final Map<String, Iterator<String>> answers = new HashMap<String,Iterator<String>>();
		answers.putAll(genericAnswers());
		parameters.forEach((k,v)->{
			answers.put(k, Iterables.cycle(v).iterator());
		});
		return answers;
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
