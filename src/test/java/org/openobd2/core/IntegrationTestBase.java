package org.openobd2.core;

import java.io.IOException;

import org.openobd2.core.connection.Connection;
import org.openobd2.core.connection.bt.BluetoothConnection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class IntegrationTestBase {

	protected Connection openConnection() {
		try {
			return BluetoothConnection.builder().adapter("AABBCC112233").build();
		} catch (IOException e) {
			log.error("Failed to open BT channel",e);
		}
		return null;
	}

}
