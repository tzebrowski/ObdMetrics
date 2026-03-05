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
package org.obd.metrics.transport.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.transport.AdapterConnection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class SmartMockAdapterConnection implements AdapterConnection {

	@AllArgsConstructor
	static final class OutStream extends ByteArrayOutputStream {

		private final Map<String, Iterator<String>> requestResponse;
		private final MutableByteArrayInputStream in;
		private final long writeTimeout;
		private final boolean simulateWriteError;

		@Getter
		private final LinkedBlockingDeque<String> recordedQueries = new LinkedBlockingDeque<>();

		private void processCommandBytes(byte[] bytes) throws IOException {
			if (simulateWriteError) {
				throw new IOException("Write exception");
			}
			if (bytes == null || bytes.length == 0) {
				return;
			}

			final String command = new String(bytes).trim().replaceAll("\r", "");
			if (command.isEmpty()) {
				return;
			}

			if (log.isTraceEnabled()) {
				log.trace("In command: {}", command);
			}

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

				if (log.isTraceEnabled()) {
					log.trace("Matches: {} = {}", command, collection);
				}

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

	private final OutStream output;
	private final MutableByteArrayInputStream input;
	private final boolean simulateErrorInReconnect;

	public BlockingDeque<String> recordedQueries() {
		return output.recordedQueries;
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