package org.openobd2.core.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class BluetoothStreams extends Streams {

	final StreamConnection streamConnection;

	@Override
	public InputStream getInputStream() throws IOException {
		return this.streamConnection.openInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return this.streamConnection.openDataOutputStream();
	}

}
