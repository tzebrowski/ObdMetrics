package org.obd.metrics.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.obd.metrics.command.Command;
import org.obd.metrics.raw.RawMessage;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultConnector implements Connector {

	@Getter
	private boolean faulty;

	@NonNull
	private OutputStream out;

	@NonNull
	private InputStream in;

	@NonNull
	private final AdapterConnection connection;
	private final byte[] buffer = new byte[96];

	DefaultConnector(final AdapterConnection connection) throws IOException {
		this.connection = connection;
		this.out = connection.openOutputStream();
		this.in = connection.openInputStream();
		Arrays.fill(buffer, 0, buffer.length, (byte) 0);
	}

	@Override
	public void close() {
		log.info("Closing streams.");
		faulty = false;
		try {
			out.close();
		} catch (IOException e) {
		}
		try {
			in.close();
		} catch (IOException e) {
		}

		try {
			connection.close();
		} catch (IOException e) {
		}

	}

	private long tts = 0;

	@Override
	public synchronized void transmit(@NonNull Command command) {
		tts = System.currentTimeMillis();
		if (isFaulty()) {
			log.warn("Previous IO failed. Cannot perform another IO operation");
		} else {
			try {
				if (log.isTraceEnabled()) {
					log.trace("TX: {}", command.getQuery());
				}
				out.write(command.getData());

			} catch (IOException e) {
				log.error("Failed to transmit command: {}", command, e);
				reconnect();
			}
		}
	}

	@Override
	public synchronized RawMessage receive() {
		if (isFaulty()) {
			log.warn("Previous IO failed. Cannot perform another IO operation");
		} else {
			try {

				short cnt = 0;
				int nextByte;
				char characterRead;

				while ((nextByte = in.read()) > -1 && (characterRead = (char) nextByte) != '>'
				        && cnt != buffer.length) {
					if (Characters.isCharacterAllowed(characterRead)) {
						buffer[cnt++] = (byte) Character.toUpperCase(characterRead);
					}
				}

				short start = 0;
				if ((char) buffer[0] == 'S' &&
				        (char) buffer[1] == 'E' &&
				        (char) buffer[2] == 'A' &&
				        (char) buffer[3] == 'R') {
					// SEARCHING...
					start = 12;
					cnt = (short) (cnt - start);
				}

				final RawMessage raw = RawMessage.wrap(Arrays.copyOfRange(buffer, start, start + cnt));

				Arrays.fill(buffer, 0, cnt, (byte) 0);

				tts = System.currentTimeMillis() - tts;

				if (log.isTraceEnabled()) {
					log.trace("RX: {}, processing time: {}ms", raw.getMessage(), tts);
				}
				return raw;
			} catch (IOException e) {
				log.error("Failed to receive data", e);
				reconnect();
			}
		}
		return RawMessage.EMPTY_MESSAGE;
	}

	void reconnect() {
		log.error("Connection is broken. Reconnecting...");
		try {
			connection.reconnect();
			in = connection.openInputStream();
			out = connection.openOutputStream();
			faulty = false;
		} catch (IOException e) {
			faulty = true;
		}
	}
}