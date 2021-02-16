package org.obd.metrics.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.connection.Connection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class MockConnection implements Connection {

	@AllArgsConstructor
	static final class Out extends ByteArrayOutputStream {
		final Map<String, String> reqResp;
		final In in;
		final long writeTimeout;
		final boolean simulateWriteError;

		@Override
		public void write(byte[] buff) throws IOException {
			if (simulateWriteError) {
				throw new IOException("blabla");
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

		public In(long readTimeout) {
			super("".getBytes());
			this.readTimeout = readTimeout;
		}

		@Override
		public synchronized int read() {
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
	private boolean simulateWriteError;

	private boolean closedConnnection;

	@Builder
	public static MockConnection build(@Singular("commandReply") Map<String, String> parameters, long writeTimeout,
			long readTimeout, boolean simulateWriteError, boolean closedConnnection) {

		final MockConnection connection = new MockConnection();
		connection.simulateWriteError = simulateWriteError;
		connection.closedConnnection = closedConnnection;
		connection.input = new In(readTimeout);
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
	public boolean isClosed() {
		return closedConnnection;
	}

	@Override
	public void reconnect() throws IOException {
		if (simulateWriteError) {
			throw new IOException("bleble");
		}
	}

	@Override
	public void close() throws IOException {

	}
}
