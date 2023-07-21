package org.obd.metrics.executor;

import org.obd.metrics.command.Command;
import org.obd.metrics.transport.Connector;

@FunctionalInterface
public interface CommandHandler {
	final String ERR_TIMEOUT = "TIMEOUT";
	final String ERR_LVRESET = "LVRESET";
	
	CommandExecutionStatus execute(Connector connector, Command command) throws Exception;
	
	static CommandHandler of() {
		return new DefaultCommandHandler();
	}
}