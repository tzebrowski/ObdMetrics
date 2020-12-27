package org.openobd2.core.streams.bt;

import java.io.IOException;

import javax.microedition.io.StreamConnection;

import org.openobd2.core.streams.Streams;

import com.intel.bluetooth.MicroeditionConnector;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public final class BluetoothStream {

	@Builder
	public static Streams of(@NonNull final String adapter) throws IOException {

		log.info("Opening connection to bluetooth device: {}", adapter);
		final String serverURL = String.format("btspp://%s:1;authenticate=false;encrypt=false;master=false", adapter);
		final StreamConnection openConnection = (StreamConnection) MicroeditionConnector.open(serverURL);
		log.info("Connection to bluetooth device: {} is opened: {}", adapter, openConnection);

		return new BluetoothStreams(openConnection);
	}

}
