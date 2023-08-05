package org.obd.metrics.transport;

import java.io.Closeable;
import java.io.IOException;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.command.Command;
import org.obd.metrics.context.Service;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.Builder;

public interface Connector extends Closeable, Service {
	static final int BUFFER_SIZE = 96;

	boolean isFaulty();

	void transmit(Command command);

	ConnectorResponse receive();

	@Builder
	static Connector create(final AdapterConnection connection, final Adjustments adjustments) throws IOException {
		connection.connect();
		return new StreamConnector(connection, adjustments);
	}
}