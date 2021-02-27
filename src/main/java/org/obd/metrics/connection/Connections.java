package org.obd.metrics.connection;

import java.io.Closeable;
import java.io.IOException;

import org.obd.metrics.command.Command;

import lombok.Builder;
import lombok.NonNull;

public interface Connections extends Closeable {

	boolean isFaulty();

	Connections transmit(Command command);

	String receive();

	@Builder
	static Connections connect(@NonNull Connection connection) throws IOException {
		connection.connect();
		return new DefaultConnections(false,
		        connection.openOutputStream(),
		        connection.openInputStream(),
		        connection);
	}
}