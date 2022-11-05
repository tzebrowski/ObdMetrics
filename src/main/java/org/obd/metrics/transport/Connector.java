package org.obd.metrics.transport;

import java.io.Closeable;
import java.io.IOException;

import org.obd.metrics.command.Command;
import org.obd.metrics.context.Service;
import org.obd.metrics.transport.message.ConnectorMessage;

import lombok.Builder;

public interface Connector extends Closeable, Service {

	boolean isFaulty();

	void transmit(Command command);

	ConnectorMessage receive();

	@Builder
	static Connector create(final AdapterConnection connection) throws IOException {
		connection.connect();
		return new SocketConnector(connection);
	}
}