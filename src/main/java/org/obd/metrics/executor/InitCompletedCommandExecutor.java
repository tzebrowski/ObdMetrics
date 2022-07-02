package org.obd.metrics.executor;

import org.obd.metrics.api.DeviceProperties;
import org.obd.metrics.command.Command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
final class InitCompletedCommandExecutor extends CommandExecutor {

	@Override
	public CommandExecutionStatus execute(ExecutionContext context, Command command) throws InterruptedException {

		log.info("Initialization is completed.");
		log.info("Found device properties: {}", context.deviceProperties);
		log.info("Found device capabilities: {}", context.deviceCapabilities);

		context.lifecycle.onRunning(new DeviceProperties(context.deviceProperties,
				context.deviceCapabilities));
		return CommandExecutionStatus.OK;
	}
}
