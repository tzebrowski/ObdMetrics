package org.obd.metrics.executor;

import org.obd.metrics.command.Command;
import org.obd.metrics.transport.Connector;

@FunctionalInterface
public interface CommandHandler {
	final String ERR_TIMEOUT = "TIMEOUT";

	CommandExecutionStatus execute(Connector connector, Command command) throws Exception;
	
	static CommandHandler of() {
		return new DefaultCommandHandler();
	}
}