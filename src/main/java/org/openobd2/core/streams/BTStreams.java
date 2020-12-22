package org.openobd2.core.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class BTStreams implements Streams {

	final StreamConnection streamConnection;

	public InputStream getInputStream() throws IOException {
		return this.streamConnection.openInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return this.streamConnection.openDataOutputStream();
	}

}
