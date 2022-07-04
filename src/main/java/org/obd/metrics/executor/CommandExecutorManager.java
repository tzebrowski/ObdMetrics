package org.obd.metrics.executor;

import java.util.Map;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.InitCompletedCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandExecutorManager {

	private final Map<Class<? extends Command>, ? extends CommandExecutor> executors = Map.of(DelayCommand.class,
			new DelayCommandExecutor(), InitCompletedCommand.class, new InitCompletedCommandExecutor(),
			QuitCommand.class, new QuitCommandExecutor());

	private final CommandExecutor fallback = new ObdCommandExecutor();

	public CommandExecutionStatus run(Connector connector, Command command) throws Exception {

		log.trace("Executing the command: {}", command);
		return findCommandExecutor(command).execute(connector, command);
	}

	private CommandExecutor findCommandExecutor(Command command) {
		CommandExecutor commandExecutor = null;
		if (executors.containsKey(command.getClass())) {
			commandExecutor = executors.get(command.getClass());
		} else {
			commandExecutor = fallback;
		}
		return commandExecutor;
	}
}