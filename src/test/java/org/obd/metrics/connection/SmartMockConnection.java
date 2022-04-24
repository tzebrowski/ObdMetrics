package org.obd.metrics.connection;

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
import org.obd.metrics.transport.AdapterConnection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SmartMockConnection implements AdapterConnection {

	@AllArgsConstructor
	static final class Out extends ByteArrayOutputStream {
		final MultiValuedMap<String, String> requestResponse;
		final MutableByteArrayInputStream in;
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

	private Out output;
	private MutableByteArrayInputStream input;
	private boolean simulateErrorInReconnect = false;

	@Builder
	public static SmartMockConnection build(Query query, int numberOfEntries, long writeTimeout,
	        long readTimeout, boolean simulateWriteError, boolean simulateReadError, boolean simulateErrorInReconnect) {

		final SmartMockConnection connection = new SmartMockConnection();
		connection.simulateErrorInReconnect = simulateErrorInReconnect;
		connection.input = new MutableByteArrayInputStream(readTimeout, simulateReadError);
		connection.output = new Out(generateAnswers(query, numberOfEntries), connection.input, writeTimeout,
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

	private static MultiValuedMap<String, String> generateAnswers(Query query, int numberOfEntries) {
		final MultiValuedMap<String, String> requestResponse = new ArrayListValuedHashMap<>();
		requestResponse.putAll(GenericAnswers.genericAnswers());
		requestResponse.putAll(new EcuAnswerGenerator().generate(query, numberOfEntries));
		return requestResponse;
	}
}
