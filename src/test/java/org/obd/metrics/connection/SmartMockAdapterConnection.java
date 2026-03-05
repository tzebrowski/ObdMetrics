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

import org.obd.metrics.api.CommandsSuplier;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.codec.generator.GeneratorPolicy;
import org.obd.metrics.codec.generator.Strategy;
import org.obd.metrics.generator.EcuMultiFrameGenerator;
import org.obd.metrics.test.PIDsRegistry;
import org.obd.metrics.transport.AdapterConnection;

import com.google.common.collect.Iterables;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SmartMockAdapterConnection implements AdapterConnection {

	private static final Map<String, Iterator<String>> genericAnswers() {
		final Map<String, Iterator<String>> requestResponse = new HashMap<>();
		requestResponse.put("ATZ", Iterables.cycle("connected?").iterator());
		requestResponse.put("ATL0", Iterables.cycle("atzelm327v1.5").iterator());
		requestResponse.put("ATH0", Iterables.cycle("ath0ok").iterator());
		requestResponse.put("ATE0", Iterables.cycle("ate0ok").iterator());
		requestResponse.put("ATSP0", Iterables.cycle("ok").iterator());
		requestResponse.put("AT I", Iterables.cycle("elm327v1.5").iterator());
		requestResponse.put("AT @1", Iterables.cycle("obdiitors232interpreter").iterator());
		requestResponse.put("AT @2", Iterables.cycle("?").iterator());
		requestResponse.put("AT DP", Iterables.cycle("auto").iterator());
		requestResponse.put("AT DPN", Iterables.cycle("a0").iterator());
		requestResponse.put("AT RV", Iterables.cycle("11.8v").iterator());
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

		private void processCommandBytes(byte[] buff) throws IOException {
			if (simulateWriteError) {
				throw new IOException("Write exception");
			}
			if (buff == null || buff.length == 0) {
				return;
			}

			final String command = new String(buff).trim().replaceAll("\r", "");
			if (command.isEmpty()) {
				return;
			}

			log.trace("In command: {}", command);
			recordedQueries.addLast(command);

			if (writeTimeout > 0) {
				try {
					TimeUnit.MILLISECONDS.sleep(writeTimeout);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			if (requestResponse.containsKey(command)) {
				final Iterator<String> collection = requestResponse.get(command);
				log.trace("Matches: {} = {}", command, collection);
				if (collection != null && collection.hasNext()) {
					in.update(collection.next());
				}
			}
		}

		@Override
		public void write(byte[] buff) throws IOException {
			processCommandBytes(buff);
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) {
			byte[] chunk = new byte[len];
			System.arraycopy(b, off, chunk, 0, len);
			try {
				processCommandBytes(chunk);
			} catch (IOException e) {
				throw new RuntimeException(e); // ByteArrayOutputStream methods don't throw checked exceptions
			}
		}

		@Override
		public synchronized void write(int b) {
			super.write(b);
			// If writing byte-by-byte, wait for the carriage return to process the command
			if (b == '\r') {
				try {
					processCommandBytes(this.toByteArray());
					this.reset(); // clear buffer after processing
				} catch (IOException e) {
					throw new RuntimeException(e);
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

	public static AdapterConnection get(final PIDsRegistry registry, final Adjustments optional, final Query query,
			final Init init, Strategy strategy) {

		final GeneratorPolicy policy = GeneratorPolicy.builder().enabled(true).strategy(strategy).build();
		final EcuMultiFrameGenerator multiFrameGenerator = new EcuMultiFrameGenerator(registry);
		SmartMockAdapterConnectionBuilder builder = SmartMockAdapterConnection.builder();
		final CommandsSuplier commandsSuplier = new CommandsSuplier(registry, optional, query, init);

		commandsSuplier.get().forEach(e -> {
			final String ecuQuery = e.getQuery();
			log.info("Generating ECU answers for: {}", ecuQuery);
			int count = 5;
			final List<String> answers = multiFrameGenerator.generateAnswers(ecuQuery, count, policy);
			log.info("Built {} ECU answers for query {}", count, ecuQuery);
			builder.requestResponse(ecuQuery, answers);
		});

		return builder.build();
	}

	@Builder
	public static SmartMockAdapterConnection build(
			@Singular("requestResponse") Map<String, List<String>> requestResponse, long writeTimeout, long readTimeout,
			boolean simulateWriteError, boolean simulateReadError, boolean simulateErrorInReconnect) {

		final SmartMockAdapterConnection connection = new SmartMockAdapterConnection();
		connection.simulateErrorInReconnect = simulateErrorInReconnect;
		connection.input = new MutableByteArrayInputStream(readTimeout, simulateReadError);
		connection.output = new Out(wrap(requestResponse), connection.input, writeTimeout, simulateWriteError);
		return connection;
	}

	private static Map<String, Iterator<String>> wrap(Map<String, List<String>> parameters) {
		final Map<String, Iterator<String>> answers = new HashMap<String, Iterator<String>>();
		answers.putAll(genericAnswers());
		parameters.forEach((k, v) -> {
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