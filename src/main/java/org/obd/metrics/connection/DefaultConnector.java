package org.obd.metrics.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.obd.metrics.command.Command;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultConnector implements Connector {

	private static final String MSG_SEARCHING = "SEARCHING...";

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
				log.info("TX: {}", command.getQuery());
				out.write((command.getQuery() + "\r").getBytes());

			} catch (IOException e) {
				log.trace("Failed to transmit command: {}", command, e);
				reconnect();
			}
		}
	}

	@Override
	public synchronized String receive() {
		if (isFaulty()) {
			log.warn("Previous IO failed. Cannot perform another IO operation");
		} else {
			try {
				final StringBuilder res = new StringBuilder();
				byte byteRead;
				char characterRead;

				while ((byteRead = (byte) in.read()) > -1 && (characterRead = (char) byteRead) != '>') {
					if (characterRead != '\t' && characterRead != '\n' && characterRead != '\r'
					        && characterRead != ' ') {
						res.append(characterRead);
					}
				}

				final String data = res.toString().replace(MSG_SEARCHING, "").toLowerCase();
				log.info("RX: {}", data);

				return data;
			} catch (IOException e) {
				log.trace("Failed to receive data", e);
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