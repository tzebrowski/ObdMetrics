package org.obd.metrics.executor;

import java.util.concurrent.TimeUnit;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.transport.Connector;

final class DelayCommandExecutor implements CommandExecutor {

	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) throws InterruptedException {
		final DelayCommand delayCommand = (DelayCommand) command;
		TimeUnit.MILLISECONDS.sleep(delayCommand.getDelay());
		return CommandExecutionStatus.OK;
	}
}
