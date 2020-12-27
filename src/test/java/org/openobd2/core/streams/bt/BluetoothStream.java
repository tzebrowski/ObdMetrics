package org.openobd2.core.streams.bt;

import java.io.IOException;

import javax.microedition.io.StreamConnection;

import org.openobd2.core.streams.Streams;

import com.intel.bluetooth.MicroeditionConnector;

import lombok.Builder;
import lombok.NonNull;

public interface BluetoothStream {

	@Builder
	public static Streams of(@NonNull final String adapter) throws IOException {

		final String serverURL = String.format("btspp://%s:1;authenticate=false;encrypt=false;master=false", adapter);
		final StreamConnection openConnection = (StreamConnection) MicroeditionConnector.open(serverURL);
		return new BluetoothStreams(openConnection);
	}

}
