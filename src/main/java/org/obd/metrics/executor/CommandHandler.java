package org.obd.metrics.executor;

import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.context.Context;
import org.obd.metrics.transport.Connector;

@FunctionalInterface
public interface CommandHandler {

	CommandExecutionStatus execute(Connector connector, Command command) throws Exception;
	
	static CommandHandler of() {
		return new DefaultCommandHandler(Context.instance().resolve(ConnectorResponseBuffer.class).get());
	}
}