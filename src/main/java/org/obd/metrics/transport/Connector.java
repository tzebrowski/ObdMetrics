package org.obd.metrics.transport;

import java.io.Closeable;
import java.io.IOException;

import org.obd.metrics.command.Command;
import org.obd.metrics.raw.RawMessage;

import lombok.Builder;

public interface Connector extends Closeable {

	boolean isFaulty();

	void transmit(Command command);

	RawMessage receive();

	@Builder
	static Connector create(final AdapterConnection connection) throws IOException {
		connection.connect();
		return new DefaultConnector(connection);
	}
}