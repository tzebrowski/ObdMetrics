package org.obd.metrics.executor;

import org.obd.metrics.command.Command;
import org.obd.metrics.transport.Connector;

public interface CommandExecutor {

	CommandExecutionStatus execute(Connector connector, Command command) throws Exception;
}