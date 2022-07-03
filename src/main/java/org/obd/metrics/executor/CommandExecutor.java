package org.obd.metrics.executor;

import org.obd.metrics.command.Command;

public abstract class CommandExecutor {

	public abstract CommandExecutionStatus execute(Command command) throws Exception;
}