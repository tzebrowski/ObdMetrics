package org.obd.metrics.executor;

import java.util.concurrent.TimeUnit;

import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.DelayCommand;


final class DelayCommandExecutor extends CommandExecutor {

	@Override
	public CommandExecutionStatus execute(ExecutionContext context,Command command) throws InterruptedException {
		final DelayCommand delayCommand = (DelayCommand) command;
		TimeUnit.MILLISECONDS.sleep(delayCommand.getDelay());
		return CommandExecutionStatus.OK;
	}
}
