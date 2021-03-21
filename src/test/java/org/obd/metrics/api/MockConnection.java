package org.obd.metrics.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.connection.StreamConnection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class MockConnection implements StreamConnection {

	@AllArgsConstructor
	static final class Out extends ByteArrayOutputStream {
		final Map<String, String> reqResp;
		final In in;
		final long writeTimeout;
		final boolean simulateWriteError;

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

	@Builder
	public static MockConnection build(@Singular("commandReply") Map<String, String> parameters, long writeTimeout,
	        long readTimeout, boolean simulateWriteError, boolean simulateReadError, boolean simulateErrorInReconnect) {

		final MockConnection connection = new MockConnection();
		connection.simulateErrorInReconnect = simulateErrorInReconnect;
		connection.input = new In(readTimeout, simulateReadError);
		connection.output = new Out(parameters, connection.input, writeTimeout, simulateWriteError);
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
}
