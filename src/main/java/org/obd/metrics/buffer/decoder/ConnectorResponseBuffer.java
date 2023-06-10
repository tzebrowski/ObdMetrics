package org.obd.metrics.buffer.decoder;

import org.obd.metrics.context.Service;

public interface ConnectorResponseBuffer extends Service {
	
	ConnectorResponseBuffer clear();

	ConnectorResponseBuffer addLast(ConnectorResponseWrapper command);

	ConnectorResponseWrapper get() throws InterruptedException;

	static ConnectorResponseBuffer instance() {
		return new DefaultConnectorResponseBuffer();
	}
}