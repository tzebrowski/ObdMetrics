package org.obd.metrics.executor;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.transport.Connector;

public abstract class CommandExecutor {

	static final CommandExecutor OBD_EXECUTOR = new ObdCommandExecutor();

	public abstract ExecutionStatus execute(ExecutionContext context, Command command) throws Exception;

	public static CommandExecutor findBy(Command command, final Connector connector) {
		CommandExecutor executor = null;

		if (QuitCommand.class.isInstance(command)) {
			executor = new QuitCommandExecutor();
		} else if (DelayCommand.class.isInstance(command)) {
			executor = new DelayCommandExecutor();
		} else if (InitCompletedCommand.class.isInstance(command)) {
			executor = new InitCompletedCommandExecutor();
		} else {
			executor = OBD_EXECUTOR;
		}

		return executor;
	}
}