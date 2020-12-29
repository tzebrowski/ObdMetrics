package org.openobd2.core.channel.bt;

import java.io.IOException;

import javax.microedition.io.StreamConnection;

import org.openobd2.core.channel.Channel;

import com.intel.bluetooth.MicroeditionConnector;

import lombok.Builder;
import lombok.NonNull;

public interface BluetoothStream {

	@Builder
	public static Channel of(@NonNull final String adapter) throws IOException {

		final String serverURL = String.format("btspp://%s:1;authenticate=false;encrypt=false;master=false", adapter);
		final StreamConnection openConnection = (StreamConnection) MicroeditionConnector.open(serverURL,MicroeditionConnector.READ_WRITE,true);
		return new BluetoothStreams(openConnection);
	}

}
