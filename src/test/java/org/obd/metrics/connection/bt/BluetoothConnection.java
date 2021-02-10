package org.obd.metrics.connection.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import org.obd.metrics.connection.Connection;

import com.intel.bluetooth.MicroeditionConnector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class BluetoothConnection implements Connection {

	final String adapterName;

	StreamConnection streamConnection;

	BluetoothConnection(final String adapterName) {
		this.adapterName = adapterName;
	}

	@Builder()
	public static Connection of(@NonNull final String adapter) throws IOException {
		return new BluetoothConnection(adapter);
	}

	@Override
	public void connect() throws IOException {
		final String serverURL = String.format("btspp://%s:1;authenticate=false;encrypt=false;master=false",
				adapterName);
		this.streamConnection = (StreamConnection) MicroeditionConnector.open(serverURL,
				MicroeditionConnector.READ_WRITE, true);
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return this.streamConnection.openInputStream();
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return this.streamConnection.openDataOutputStream();
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public void close() throws IOException {
		streamConnection.close();
	}

	@Override
	public void reconnect() throws IOException {

	}
}
