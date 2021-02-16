package org.obd.metrics.api.integration;

import java.io.IOException;

import org.obd.metrics.connection.Connection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class IntegrationTestBase {

	protected Connection openConnection() {
		try {
			return BluetoothConnection.builder().adapter("AABBCC112233").build();
		} catch (IOException e) {
			log.error("Failed to open BT channel", e);
		}
		return null;
	}
}
