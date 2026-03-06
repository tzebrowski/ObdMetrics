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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.AdapterConnection;

import com.google.common.collect.Iterables;

import lombok.AllArgsConstructor;
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
		public void write(byte[] b, int off, int len) {
			byte[] chunk = new byte[len];
			System.arraycopy(b, off, chunk, 0, len);
			try {
				processCommandBytes(chunk);
			} catch (IOException e) {
				throw new RuntimeException(e); // ByteArrayOutputStream methods don't throw checked exceptions
			}
		}

		@Override
		public void write(int b) {
			super.write(b);
			
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
	private final EcuResponseGenerator ecuResponseGenerator;
	
	@Override
	public void update(List<ObdCommand> commandList) {
		if (ecuResponseGenerator == null) {
			log.error("EcuResponseGenerator is not set, do not continue.");
			return ;
		}
		
		final ExecutorService threadPool = ForkJoinPool.commonPool();
		
		final long totalStartTime = System.currentTimeMillis();
		
		final List<CompletableFuture<Void>> generationTasks = commandList.stream()
			.map(e -> CompletableFuture.runAsync(() -> {
				final String ecuQuery = e.getQuery();
				final String theadName = Thread.currentThread().getName();
				
				log.info("[{}] Generating ECU answers for: {}",theadName, ecuQuery);
				
				long queryStartTime = System.currentTimeMillis();
				final List<String> answers = ecuResponseGenerator.generateAnswers(ecuQuery);
				final long queryExecutionTime = System.currentTimeMillis() - queryStartTime;
				
				log.info("[{}] Built {} ECU answers for query {} in {} ms",theadName, answers.size(), ecuQuery, queryExecutionTime);

				output.requestResponse.put(ecuQuery, Iterables.cycle(answers).iterator());
				
			}, threadPool)) 
			.collect(Collectors.toList());

		CompletableFuture.allOf(generationTasks.toArray(new CompletableFuture[0]))
			.thenRun(() ->  {
				final long totalExecutionTime = System.currentTimeMillis() - totalStartTime;
				log.info("All background ECU answer generation completed in {} ms.", totalExecutionTime);
			});
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