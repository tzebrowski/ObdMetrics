package org.obd.metrics.api.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

import org.obd.metrics.connection.Connection;

import com.intel.bluetooth.MicroeditionConnector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class BluetoothConnection implements Connection {

	final String adapterName;

	StreamConnection streamConnection;

	public static void main(String[] args) throws BluetoothStateException {
		final LocalDevice localDevice = LocalDevice.getLocalDevice();
		final RemoteDevice[] retrieveDevices = localDevice.getDiscoveryAgent().retrieveDevices(DiscoveryAgent.CACHED);
		for (RemoteDevice rr : retrieveDevices) {
			log.info("device addr: {}", rr.getBluetoothAddress());
		}
	}

	BluetoothConnection(final String adapterName) {
		this.adapterName = adapterName;
	}

	static Connection openConnection() {
		// VLINK 001DA5215E98
		// OBD2 AABBCC112233
		return openConnection("AABBCC112233");
	}

	static Connection openConnection(String addr) {
		try {
			return BluetoothConnection.builder().adapter(addr).build();
		} catch (IOException e) {
			log.error("Failed to open BT channel", e);
		}
		return null;
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
