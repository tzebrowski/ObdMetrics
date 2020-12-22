package org.openobd2.core.streams;

import java.io.IOException;

import javax.microedition.io.StreamConnection;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public final class StreamFactory {

	public static Streams bt(final String btId) throws IOException {
		log.info("Opening connection to bluetooth device: {}", btId);
		final StreamConnection openConnection = BTManager.openConnection(btId);
		log.info("Connection to bluetooth device: {} is opened: {}", btId, openConnection);
		return new BTStreams(openConnection);
	}
}
