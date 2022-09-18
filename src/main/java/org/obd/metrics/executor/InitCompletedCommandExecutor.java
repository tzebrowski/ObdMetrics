package org.obd.metrics.executor;

import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.VehicleCapabilities;
import org.obd.metrics.command.Command;
import org.obd.metrics.context.Context;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class InitCompletedCommandExecutor implements CommandExecutor {
	private final VehicleMetadataReader metadataReader = new VehicleMetadataReader();
	private final VehicleCapabilitiesReader capabilitiesReader = new VehicleCapabilitiesReader();
	private final DiagnosticTroubleCodeReader dtcReader = new DiagnosticTroubleCodeReader();

	@SuppressWarnings("unchecked")
	InitCompletedCommandExecutor() {

		Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
			p.subscribe(metadataReader);
			p.subscribe(capabilitiesReader);
			p.subscribe(dtcReader);
		});
	}

	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) throws InterruptedException {

		log.info("Initialization process is completed.");
		log.info("Found Vehicle metadata: {}", metadataReader.getValue());
		log.info("Found Vehicle capabilities: {}", capabilitiesReader.getValue());
		log.info("Found DTC: {}", dtcReader.getValue());
		
		Context.apply( ctx -> {
			ctx.resolve(Subscription.class).apply(p -> {
				ctx.resolve(EventsPublishlisher.class).apply(e -> {
					p.onRunning(new VehicleCapabilities(metadataReader.getValue(),
							capabilitiesReader.getValue(), dtcReader.getValue()));

				});
			});
		});
		return CommandExecutionStatus.OK;
	}
}
