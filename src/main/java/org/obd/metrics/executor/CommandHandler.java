package org.obd.metrics.executor;

import org.obd.metrics.buffer.decoder.ResponseBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.context.Context;
import org.obd.metrics.transport.Connector;

@FunctionalInterface
public interface CommandHandler {

	CommandExecutionStatus execute(Connector connector, Command command) throws Exception;
	
	static CommandHandler of() {
		return new DefaultCommandHandler(Context.instance().resolve(ResponseBuffer.class).get());
	}
}