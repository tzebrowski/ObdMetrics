/** 
 * Copyright 2019-2023, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
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
public final class BluetoothConnection implements org.obd.metrics.transport.AdapterConnection {

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
			log.info("Found following BT devices: name='{}' addr='{}'", device.getFriendlyName(false), device.getBluetoothAddress());
		}
		
		for (RemoteDevice device : devices) {
			if (name.equalsIgnoreCase(device.getFriendlyName(false))) {
				return device.getBluetoothAddress();
			}
		}
		throw new IOException("Did not find the device addr");
	}

	public static org.obd.metrics.transport.AdapterConnection openConnection() throws IOException {
		return openConnection(findDeviceAddr("OBDII"));
	}

	static org.obd.metrics.transport.AdapterConnection openConnection(@NonNull String addr) throws IOException {
		log.info("Connecting to: {}", addr);
		return BluetoothConnection.builder().adapter(addr).build();
	}

	@Builder()
	public static org.obd.metrics.transport.AdapterConnection of(@NonNull final String adapter) throws IOException {
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
