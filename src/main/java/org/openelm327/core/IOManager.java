package org.openelm327.core;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.openelm327.core.command.Command;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
final class IOManager {

	final OutputStream os;
	final InputStreamReader in;

	void closeQuitly() {

		try {
			os.close();
		} catch (IOException e) {
		}
		try {
			in.close();
		} catch (IOException e) {
		}
	}

	void write(Command command) throws IOException {
		if (os == null || null == command) {
			log.warn("Stream is closed or command is null");
		} else {
			log.info("Sending command: {}", command.getValue());
			final String text = command.getValue() + "\r";
			os.write(text.getBytes());
			os.flush();
		}
	}

	String read(Command command) throws IOException {
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

			final String data = res.toString().replaceAll("\\s", "");
			log.info("Command: {}. Recieved data: {}, length: {}", command.getValue(), data, data.length());
			return data;
		}
	}
}
