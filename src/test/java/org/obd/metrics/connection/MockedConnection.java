package org.obd.metrics.connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.obd.metrics.connection.Connection;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockedConnection implements Connection {

	@AllArgsConstructor
	final class Out extends ByteArrayOutputStream {
		final Map<String, String> reqResp;

		@Override
		public void write(byte[] b) throws IOException {
			final String command = new String(b).trim().replaceAll("\r", "");
			log.trace("In command: {}", command);
			if (reqResp.containsKey(command)) {
				final String answer = reqResp.get(command);
				log.trace("Matches: {} = {}", command, answer);
				input.write(answer);
			}
		}
	}

	final class In extends ByteArrayInputStream {
		public In() {
			super("".getBytes());
		}

		void write(String buff) {
			this.buf = buff.getBytes();
			this.pos = 0;
			this.count = buf.length;
		}
	}

	private final Out output;
	private final In input = new In();

	public MockedConnection(Map<String, String> reqResp) {
		this.output = new Out(reqResp);
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
