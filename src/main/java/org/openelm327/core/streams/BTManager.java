package org.openelm327.core.streams;

import java.io.IOException;

import javax.microedition.io.StreamConnection;

import com.intel.bluetooth.MicroeditionConnector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class BTManager {
	static StreamConnection openConnection(String obdDongleId) throws IOException {
		final String serverURL = String.format("btspp://%s:1;authenticate=false;encrypt=false;master=false",
				obdDongleId);
		return (StreamConnection) MicroeditionConnector.open(serverURL);
	}
}
