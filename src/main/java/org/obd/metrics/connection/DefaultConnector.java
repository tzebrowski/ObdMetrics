package org.obd.metrics.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

	DefaultConnector(final AdapterConnection connection) throws IOException {
		this.connection = connection;
		this.out = connection.openOutputStream();
		this.in = connection.openInputStream();
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

	@Override
	public synchronized void transmit(@NonNull Command command) {
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
				final StringBuilder res = new StringBuilder();
				int nextByte;
				char characterRead;

				while ((nextByte = in.read()) > -1 && (characterRead = (char) nextByte) != '>') {
					if (Characters.isCharacterAllowed(characterRead)) {
						res.append(Character.toUpperCase(characterRead));
					}
				}

				final String message = res.toString();
				
				if (log.isTraceEnabled()) {
					log.trace("RX: {}", message);
				}
				
				return RawMessage.instance(message);
			} catch (IOException e) {
				log.error("Failed to receive data", e);
				reconnect();
			}
		}
		return null;
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