package org.obd.metrics.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class DefaultConnector implements Connector {
	protected static final char NEXT_MESSAGE_SIGNAL = '>';
	protected final ConnectorResponseFactory connectorResponsefactory;
	protected final ConnectorResponse EMPTY_MESSAGE;

	@Getter
	protected boolean faulty;

	@NonNull
	protected OutputStream out;

	@NonNull
	protected InputStream in;

	@NonNull
	protected final AdapterConnection connection;
	protected final Adjustments adjustments;

	protected final byte[] buffer;
	protected long tts = 0;
	protected boolean closed = false;

	public DefaultConnector(int bufferSize, final AdapterConnection connection, final Adjustments adjustments)
			throws IOException {
		this.buffer = new byte[bufferSize];
		this.connection = connection;
		this.adjustments = adjustments;
		this.out = connection.openOutputStream();
		this.in = connection.openInputStream();

		this.connectorResponsefactory = new ConnectorResponseFactory(bufferSize);
		this.EMPTY_MESSAGE = connectorResponsefactory.wrap(new byte[] {}, 0, 0);

		reset();
	}

	@Override
	public void close() {
		log.info("Closing streams.");
		closed = true;
		faulty = false;
		try {
			if (out != null) {
				out.close();
				out = null;
			}
		} catch (final IOException e) {
		}
		try {
			if (in != null) {
				in.close();
				in = null;
			}
		} catch (final IOException e) {
		}

		try {
			connection.close();
		} catch (final IOException e) {
		}

	}

	protected void reset() {
		Arrays.fill(buffer, 0, buffer.length, (byte) 0);
	}

	protected void reconnect() {
		if (closed) {
			log.error("Connection is closed. Do not try to reconnect.");
		} else {
			log.error("Connection is broken. Reconnecting...");
			try {
				connection.reconnect();
				in = connection.openInputStream();
				out = connection.openOutputStream();
				faulty = false;
			} catch (final IOException e) {
				faulty = true;
			}
		}
	}

}
