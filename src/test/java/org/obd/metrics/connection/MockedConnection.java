package org.obd.metrics.connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockedConnection implements Connection {

	@AllArgsConstructor
	static final class Out extends ByteArrayOutputStream {
		final Map<String, String> reqResp;
		final In in;
		private long writeTimeout;

		@Override
		public void write(byte[] buff) throws IOException {
			final String command = new String(buff).trim().replaceAll("\r", "");
			log.trace("In command: {}", command);

			try {
				TimeUnit.MILLISECONDS.sleep(writeTimeout);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (reqResp.containsKey(command)) {
				final String answer = reqResp.get(command);
				log.trace("Matches: {} = {}", command, answer);
				in.write(answer);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return read;
		}

		void write(String data) {
			this.buf = data.getBytes();
			this.pos = 0;
			this.count = buf.length;
		}
	}

	private Out output;
	private In input;

	@Builder
	public static MockedConnection build(@Singular("parameter") Map<String, String> reqResp, long writeTimeout,
			long readTimeout) {

		final MockedConnection connection = new MockedConnection();
		System.out.println(reqResp);
		connection.input = new In(readTimeout);
		connection.output = new Out(reqResp, connection.input, writeTimeout);
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
		return false;
	}

	@Override
	public void reconnect() throws IOException {
	}

	@Override
	public void close() throws IOException {

	}
}
