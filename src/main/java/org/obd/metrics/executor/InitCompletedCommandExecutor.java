package org.obd.metrics.executor;

import org.obd.metrics.api.Context;
import org.obd.metrics.api.model.DeviceProperties;
import org.obd.metrics.api.model.Lifecycle.Subscription;
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
		
		Context.instance().lookup(Subscription.class).ifPresent(p -> {
			p.onRunning(new DeviceProperties(context.deviceProperties,
					context.deviceCapabilities));
		});
		
		return CommandExecutionStatus.OK;
	}
}
