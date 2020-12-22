package org.openobd2.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.openobd2.core.command.Command;
import org.openobd2.core.streams.Streams;

import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DeviceIO implements Closeable {
	private static final String MSG_SEARCHING = "SEARCHING...";

	private final OutputStream out;
	private final InputStreamReader in;

	private DeviceIO(final OutputStream out, final InputStreamReader in) {
		this.in = in;
		this.out = out;
	}

	@Builder
	static DeviceIO build(final Streams streams) throws IOException {

		if (null == streams) {
			log.error("No streams provided.");
			return null;
		} else {
			return new DeviceIO(streams.getOutputStream(), new InputStreamReader(streams.getInputStream()));
		}
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

	void write(@NonNull Command command) throws IOException {
		if (out == null || null == command) {
			log.warn("Stream is closed or command is null");
		} else {
			log.debug("Sending command: {}", command.getQuery());
			final String text = command.getQuery() + "\r";
			out.write(text.getBytes());
			out.flush();
		}
	}

	String read() throws IOException {
		if (in == null) {
			log.warn("Stream is closed or command is null");
			return null;
		} else {

			final StringBuilder res = new StringBuilder();
			byte byteRead;
			char characterRead;

			while ((byteRead = (byte) in.read()) > -1 && (characterRead = (char) byteRead) != '>') {
				if (characterRead != '\n' && characterRead != '\r') {
					res.append(characterRead);
				}
			}

			final String data = res.toString().trim().replace(MSG_SEARCHING, "");
			log.debug(" Received data: {}, length: {}", data, data.length());
			return data;
		}
	}
}
