package org.obd.metrics.executor;

import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseWrapper;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.pool.ObjectAllocator;
import org.obd.metrics.transport.Connector;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class ObdCommandHandler implements CommandHandler {
	
	private final ConnectorResponseBuffer responseBuffer;

	private final static ObjectAllocator<ConnectorResponseWrapper> allocator = 
			ObjectAllocator.of(
					ObjectAllocator.Strategy.Circular,
					ConnectorResponseWrapper.class, 255);
	
	@SuppressWarnings("unchecked")
	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) {
		connector.transmit(command);
		final ConnectorResponse connectorResponse = connector.receive();

		if (connectorResponse.isEmpty()) {
			log.debug("Received no data");
		} else if (connectorResponse.isError()) {
			log.error("Receive device error: {}", connectorResponse.getMessage());
			return new CommandExecutionStatus(connectorResponse.getMessage());
		} else if (connectorResponse.isTimeout()) {
			log.error("Device is timeouting. ");
			return CommandExecutionStatus.ERR_TIMEOUT;
		} else if (connectorResponse.isLowVoltageReset()) {
			log.error("Received low voltage error.");
			return CommandExecutionStatus.ERR_LVRESET;
		} else if (command instanceof BatchObdCommand) {
			final BatchObdCommand batch = (BatchObdCommand) command;
			batch.getCodec().decode(connectorResponse).forEach(this::handle);
		} else if (command instanceof ObdCommand) {
			handle((ObdCommand) command, connectorResponse);
		} else {
			Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
				// release here the message
				p.onNext(Reply.builder().command(command).raw(connectorResponse).build());
			});
		}
		return CommandExecutionStatus.OK;
	}

	private void handle(final ObdCommand command, final ConnectorResponse connectorResponse) {
		final ConnectorResponseWrapper allocate = allocator.allocate();
		allocate.setCommand(command);
		allocate.setConnectorResponse(connectorResponse);
		responseBuffer.addLast(allocate);
	}
}
