package org.obd.metrics.connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.obd.metrics.transport.AdapterConnection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MockAdapterConnection implements AdapterConnection {

	@AllArgsConstructor
	static final class Out extends ByteArrayOutputStream {
		final Map<String, String> requestResponse;
		final MutableByteArrayInputStream in;
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

				if (requestResponse.containsKey(command)) {
					final String answer = requestResponse.get(command);
					log.trace("Matches: {} = {}", command, answer);
					in.update(answer);
				}
			}
		}
	}

	private Out output;
	private MutableByteArrayInputStream input;
	private boolean simulateErrorInReconnect = false;

	public Set<String> recordedQueries() {
		return output.recordedQueries;
	}

	@Builder
	public static MockAdapterConnection build(@Singular("requestResponse") Map<String, String> requestResponse,
	        long writeTimeout,
	        long readTimeout, boolean simulateWriteError, boolean simulateReadError, boolean simulateErrorInReconnect) {

		final MockAdapterConnection connection = new MockAdapterConnection();
		connection.simulateErrorInReconnect = simulateErrorInReconnect;
		connection.input = new MutableByteArrayInputStream(readTimeout, simulateReadError);
		connection.output = new Out(wrap(requestResponse), connection.input, writeTimeout, simulateWriteError);
		return connection;
	}

	private static Map<String, String> wrap(Map<String, String> parameters) {
		final Map<String, String> genericAnswers = new HashMap<>();
		genericAnswers.putAll(GenericAnswers.genericAnswers());
		genericAnswers.putAll(parameters);// override
		return genericAnswers;
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
