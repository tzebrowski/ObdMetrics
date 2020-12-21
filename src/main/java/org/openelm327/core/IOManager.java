package org.openelm327.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.openelm327.core.command.ATCommand;
import org.openelm327.core.streams.Streams;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class IOManager {

	final OutputStream out;
	final InputStreamReader in;
	Streams streams;

	IOManager(final OutputStream out, final InputStreamReader in) {
		this.in = in;
		this.out = out;
	}

	@Builder
	static IOManager build(final Streams streams) throws IOException {

		if (null == streams) {
			log.error("No streams provided.");
			return null;
		} else {
			final OutputStream out = streams.getOutputStream();
			final InputStreamReader in = new InputStreamReader(streams.getInputStream());
			final IOManager ioManager = new IOManager(out, in);
			return ioManager;
		}
	}

	void close() {

		try {
			out.close();
		} catch (IOException e) {
		}
		try {
			in.close();
		} catch (IOException e) {
		}
	}

	void write(ATCommand command) throws IOException {
		if (out == null || null == command) {
			log.warn("Stream is closed or command is null");
		} else {
			log.info("Sending command: {}", command.getValue());
			final String text = command.getValue() + "\r";
			out.write(text.getBytes());
			out.flush();
		}
	}

	String read(ATCommand command) throws IOException {
		if (in == null || null == command) {
			log.warn("Stream is closed or command is null");
			return null;
		} else {

			final StringBuilder res = new StringBuilder();
			byte byteRead;
			char characterRead;

			while ((byteRead = (byte) in.read()) > -1 && (characterRead = (char) byteRead) != '>') {
				res.append(characterRead);
			}
			final String data = res.toString().trim().replace("\\n", "").replaceAll("\\r", "");
			log.info("Command: {}. Recieved data: {}, length: {}", command.getValue(), data, data.length());
			return data;
		}
	}
}
