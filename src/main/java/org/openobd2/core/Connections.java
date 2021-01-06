package org.openobd2.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openobd2.core.command.Command;
import org.openobd2.core.connection.Connection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class Connections implements Closeable {

	private static final String MSG_SEARCHING = "SEARCHING...";

	@Getter
	private boolean faulty;

	private OutputStream out;
	private InputStream in;
	private final Connection connection;

	@Builder
	public static Connections connect(@NonNull Connection connection) throws IOException {
		connection.init();
		return new Connections(false, connection.openOutputStream(), connection.openInputStream(), connection);
	}

	@Override
	public void close() {
		log.info("Closing streams.");
		faulty = false;
		try {
			if (out != null) {
				out.close();
			}
		} catch (Exception e) {
		}
		try {
			if (in != null) {
				in.close();
			}
		} catch (Exception e) {
		}

		try {
			connection.close();
		} catch (IOException e) {
		}

	}

	public synchronized void transmit(@NonNull Command command) {
		if (out == null) {
			log.trace("Stream is closed or command is null");
		} else if (connection.isClosed()) {
			log.warn("Socket is closed");
		} else if (isFaulty()) {
			log.warn("Previous IO failed. Cannot perform another IO operation");
		} else {
			try {
				log.debug("TX: {}", command.getQuery());
				out.write(command.getQuery());
				// out.flush();
			} catch (IOException e) {
				log.trace("Failed to transmit command: {}", command, e);
				reconnect();
			}
		}
	}

	public synchronized String receive() {
		if (in == null) {
			log.warn("Stream is closed");
		} else if (connection.isClosed()) {
			log.warn("Socket is closed");
		} else if (isFaulty()) {
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
				log.debug("RX: {}", data);
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
		} catch (IOException e1) {
			faulty = true;
		}
	}
}