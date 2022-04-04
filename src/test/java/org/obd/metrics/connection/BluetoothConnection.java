package org.obd.metrics.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.intel.bluetooth.MicroeditionConnector;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class BluetoothConnection implements org.obd.metrics.connection.AdapterConnection {

	final String adapterName;

	StreamConnection streamConnection;

	@Test
	public void findDeviceTest() throws IOException {
		Assertions.assertThat(findDeviceAddr("OBDII")).isEqualTo("AABBCC112233");
	}

	static String findDeviceAddr(String name) throws IOException {
		final LocalDevice localDevice = LocalDevice.getLocalDevice();
		RemoteDevice[] devices = localDevice.getDiscoveryAgent().retrieveDevices(DiscoveryAgent.CACHED);
		for (RemoteDevice device : devices) {
			log.info("BT name: {} addr: {}", device.getFriendlyName(false), device.getBluetoothAddress());
			if (name.equalsIgnoreCase(device.getFriendlyName(false))) {
				return device.getBluetoothAddress();
			}
		}
		throw new IOException("Did not find the device addr");
	}

	public static org.obd.metrics.connection.AdapterConnection openConnection() throws IOException {
		return openConnection(findDeviceAddr("OBDII"));
	}

	static org.obd.metrics.connection.AdapterConnection openConnection(@NonNull String addr) throws IOException {
		log.info("Connecting to: {}", addr);
		return BluetoothConnection.builder().adapter(addr).build();
	}

	@Builder()
	public static org.obd.metrics.connection.AdapterConnection of(@NonNull final String adapter) throws IOException {
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
	public void close() throws IOException {
		streamConnection.close();
	}

	@Override
	public void reconnect() throws IOException {

	}
}
