package org.obd.metrics.executor;

import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.DeviceProperties;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.command.Command;
import org.obd.metrics.context.Context;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class InitCompletedCommandExecutor implements CommandExecutor {
	private final DevicePropertiesReader devicePropertiesReader = new DevicePropertiesReader();
	private final DeviceCapabilitiesReader deviceCapabilitiesReader = new DeviceCapabilitiesReader();

	@SuppressWarnings("unchecked")
	InitCompletedCommandExecutor() {

		Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
			p.subscribe(devicePropertiesReader);
			p.subscribe(deviceCapabilitiesReader);
		});
	}

	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) throws InterruptedException {

		log.info("Initialization is completed.");
		log.info("Found device properties: {}", devicePropertiesReader.getProperties());
		log.info("Found device capabilities: {}", deviceCapabilitiesReader.getCapabilities());

		Context.instance().resolve(Subscription.class).apply(p -> {

			Context.instance().resolve(EventsPublishlisher.class).apply(e -> {
				p.onRunning(new DeviceProperties(devicePropertiesReader.getProperties(),
						deviceCapabilitiesReader.getCapabilities()));

			});
		});

		return CommandExecutionStatus.OK;
	}
}
