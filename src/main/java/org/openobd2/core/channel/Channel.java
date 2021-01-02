package org.openobd2.core.channel;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openobd2.core.command.Command;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Channel implements Closeable {

	public abstract InputStream getInputStream() throws IOException;

	public abstract OutputStream getOutputStream() throws IOException;

	private static final String MSG_SEARCHING = "SEARCHING...";

	private OutputStream out;
	private InputStream in;

	public Channel open() throws IOException {
		log.info("Opening streams");
		this.in = getInputStream();
		this.out = getOutputStream();
		return this;
	}

	@Override
	public void close() {

		log.info("Closing streams.");

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
	}

	public synchronized void transmit(@NonNull Command command) throws IOException {
		if (out == null || null == command) {
			log.warn("Stream is closed or command is null");
		} else {
			try {
				log.debug("TX: {}", command.getQuery());
				out.write(command.getQuery());
				out.flush();
			} catch (Throwable e) {
				log.trace("Failed to transmit command: {}", command, e);
			}
		}
	}

	public synchronized String receive() throws IOException {
		if (in == null) {
			log.warn("Stream is closed or command is null");
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
			} catch (Throwable e) {
				log.trace("Failed to receive data", e);
			}
		}
		return null;
	}
}