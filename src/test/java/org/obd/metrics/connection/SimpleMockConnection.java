package org.obd.metrics.connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SimpleMockConnection implements AdapterConnection {

	@AllArgsConstructor
	static final class Out extends ByteArrayOutputStream {
		final Map<String, String> reqResp;
		final In in;
		final long writeTimeout;
		final boolean simulateWriteError;
		@Getter
		private final Set<String> recordedQueries = new HashSet<>();

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
				recordedQueries.add(command);

				try {
					TimeUnit.MILLISECONDS.sleep(writeTimeout);
				} catch (InterruptedException e) {
				}

				if (reqResp.containsKey(command)) {
					final String answer = reqResp.get(command);
					log.trace("Matches: {} = {}", command, answer);
					in.update(answer);
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

	public Set<String> recordedQueries() {
		return output.recordedQueries;
	}

	@Builder
	public static SimpleMockConnection build(@Singular("commandReply") Map<String, String> parameters, long writeTimeout,
	        long readTimeout, boolean simulateWriteError, boolean simulateReadError, boolean simulateErrorInReconnect) {

		final SimpleMockConnection connection = new SimpleMockConnection();
		connection.simulateErrorInReconnect = simulateErrorInReconnect;
		connection.input = new In(readTimeout, simulateReadError);
		connection.output = new Out(wrap(parameters), connection.input, writeTimeout, simulateWriteError);
		return connection;
	}

	private static Map<String, String> wrap(Map<String, String> parameters) {
		final Map<String, String> mm = new HashMap<>();
		mm.put("ATZ", "connected?");
		mm.put("ATL0", "atzelm327v1.5");
		mm.put("ATH0", "ath0ok");
		mm.put("ATE0", "ate0ok");
		mm.put("ATSP0", "ok");
		mm.put("AT I", "elm327v1.5");
		mm.put("AT @1", "obdiitors232interpreter");
		mm.put("AT @2", "?");
		mm.put("AT DP", "auto");
		mm.put("AT DPN", "a0");
		mm.put("AT RV", "11.8v");
		mm.putAll(parameters);// override
		return mm;
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
