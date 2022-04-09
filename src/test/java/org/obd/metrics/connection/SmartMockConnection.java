package org.obd.metrics.connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.api.Query;
import org.obd.metrics.api.cache.EcuAnswerGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SmartMockConnection implements AdapterConnection {

	@AllArgsConstructor
	static final class Out extends ByteArrayOutputStream {
		final MultiValuedMap<String, String> requestResponse;
		final In in;
		final long writeTimeout;
		final boolean simulateWriteError;
		final Map<String, Integer> positions = new HashMap<String, Integer>();

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

				try {
					TimeUnit.MILLISECONDS.sleep(writeTimeout);
				} catch (InterruptedException e) {
				}

				if (requestResponse.containsKey(command)) {
					final List<String> responses = (List<String>) requestResponse.get(command);
					int index = 0;
					if (responses.size() == 1) {
						final String answer = responses.get(index);
						log.trace("Matches: {} = {}", command, answer);
						in.update(answer);
						positions.put(command, index);
					} else {
						if (positions.containsKey(command)) {
							index = positions.get(command);
							index++;
							if (index == responses.size() - 1) {
								index = 0;
							}
						} 
					}
					
					final String answer = responses.get(index);
					log.trace("Matches: {} = {}", command, answer);
					in.update(answer);
					positions.put(command, index);
				}
			}
		}
	}

	static final class In extends ByteArrayInputStream {
		private final long readTimeout;
		private final boolean simulateReadError;

		public In(long readTimeout, boolean simulateReadError) {
			super("".getBytes());
			this.readTimeout = readTimeout;
			this.simulateReadError = simulateReadError;
		}

		@Override
		public synchronized int read() {
			if (simulateReadError) {
				throw new RuntimeException("Read exception");
			}

			int read = super.read();
			try {
				TimeUnit.MILLISECONDS.sleep(readTimeout);
			} catch (InterruptedException e) {
			}
			return read;
		}

		void update(String data) {
			this.buf = data.getBytes();
			this.pos = 0;
			this.count = buf.length;
		}
	}

	private Out output;
	private In input;
	private boolean simulateErrorInReconnect = false;

	@Builder
	public static SmartMockConnection build(Query query,int numberOfEntries, long writeTimeout,
	        long readTimeout, boolean simulateWriteError, boolean simulateReadError, boolean simulateErrorInReconnect) {

		final SmartMockConnection connection = new SmartMockConnection();
		connection.simulateErrorInReconnect = simulateErrorInReconnect;
		connection.input = new In(readTimeout, simulateReadError);
		connection.output = new Out(generate(query, numberOfEntries), connection.input, writeTimeout,
		        simulateWriteError);
		return connection;
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

	private static MultiValuedMap<String, String> generate(Query query, int numberOfEntries) {
		final MultiValuedMap<String, String> requestResponse = new ArrayListValuedHashMap<>();

		requestResponse.put("ATZ", "connected?");
		requestResponse.put("ATL0", "atzelm327v1.5");
		requestResponse.put("ATH0", "ath0ok");
		requestResponse.put("ATE0", "ate0ok");
		requestResponse.put("ATSP0", "ok");
		requestResponse.put("AT I", "elm327v1.5");
		requestResponse.put("AT @1", "obdiitors232interpreter");
		requestResponse.put("AT @2", "?");
		requestResponse.put("AT DP", "auto");
		requestResponse.put("AT DPN", "a0");
		requestResponse.put("AT RV", "11.8v");

		requestResponse.putAll(new EcuAnswerGenerator().generate(query, numberOfEntries));
		return requestResponse;
	}
}
