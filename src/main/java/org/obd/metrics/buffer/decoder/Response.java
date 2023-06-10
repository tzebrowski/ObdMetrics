package org.obd.metrics.buffer.decoder;

import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@RequiredArgsConstructor
public final class Response {
	final ObdCommand command;
	final ConnectorResponse connectorResponse;
}
