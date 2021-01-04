package org.openobd2.core.channel.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import org.openobd2.core.channel.Channel;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class BluetoothChannelmpl extends Channel {

	final StreamConnection streamConnection;

	@Override
	public InputStream getInputStream() throws IOException {
		return this.streamConnection.openInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return this.streamConnection.openDataOutputStream();
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public void closeConnection() throws IOException {
		streamConnection.close();
	}
}
