package org.obd.metrics.buffer.decoder;

import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ConnectorResponseWrapper {
	private ObdCommand command;
	private ConnectorResponse connectorResponse;
}
