package org.obd.metrics.executor;

import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.VehicleCapabilities;
import org.obd.metrics.command.Command;
import org.obd.metrics.context.Context;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class InitCompletedCommandExecutor implements CommandExecutor {
	private final VehicleMetadataReader vehicleMetadataReader = new VehicleMetadataReader();
	private final VehicleCapabilitiesReader vehicleCapabilitiesReader = new VehicleCapabilitiesReader();
	private final DiagnosticTroubleCodeReader dtcReader = new DiagnosticTroubleCodeReader();

	@SuppressWarnings("unchecked")
	InitCompletedCommandExecutor() {

		Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
			p.subscribe(vehicleMetadataReader);
			p.subscribe(vehicleCapabilitiesReader);
			p.subscribe(dtcReader);
		});
	}

	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) throws InterruptedException {

		log.info("Initialization process is completed.");
		log.info("Found Vehicle metadata: {}", vehicleMetadataReader.getMetadata());
		log.info("Found Vehicle capabilities: {}", vehicleCapabilitiesReader.getCapabilities());
		log.info("Found DTC: {}", dtcReader.getCodes());

		Context.instance().resolve(Subscription.class).apply(p -> {

			Context.instance().resolve(EventsPublishlisher.class).apply(e -> {
				p.onRunning(new VehicleCapabilities(vehicleMetadataReader.getMetadata(),
						vehicleCapabilitiesReader.getCapabilities(), new DiagnosticTroubleCode(dtcReader.getCodes())));

			});
		});

		return CommandExecutionStatus.OK;
	}
}
