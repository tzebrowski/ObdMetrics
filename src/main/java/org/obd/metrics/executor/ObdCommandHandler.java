package org.obd.metrics.executor;

import java.util.Collection;

import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ObdMetric.ObdMetricBuilder;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.buffer.decoder.ConnectorResponseWrapper;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.executor.MetricValidator.MetricValidatorStatus;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.Connector;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class ObdCommandHandler implements CommandHandler {
	
	private final ConnectorResponseBuffer responseBuffer;
	private static final ConnectorResponse EMPTY_CONNECTOR_RESPONSE = ConnectorResponseFactory.empty();
	private final boolean decoderThreadEnabled = true;

	@SuppressWarnings("unchecked")
	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) {
		connector.transmit(command);
		final ConnectorResponse connectorResponse = connector.receive();

		if (connectorResponse.isEmpty()) {
			log.debug("Received no data");
		} else if (connectorResponse.isError()) {
			log.error("Receive device error: {}", connectorResponse.getMessage());

			Context.instance().resolve(Subscription.class).apply(p -> {
				p.onError(connectorResponse.getMessage(), null);
			});
		} else if (connectorResponse.isTimeout()) {
			log.error("Device is timeouting. Stopping connection.");

			Context.instance().resolve(Subscription.class).apply(p -> {
				p.onError(ERR_TIMEOUT, null);
			});
			
		} else if (command instanceof BatchObdCommand) {
			final BatchObdCommand batch = (BatchObdCommand) command;
			batch.getCodec().decode(connectorResponse).forEach(this::handle);
		} else if (command instanceof ObdCommand) {
			handle((ObdCommand) command, connectorResponse);
		} else {
			Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
				// release here the message
				p.onNext(Reply.builder().command(command).raw(connectorResponse).build());
			});
		}
		return CommandExecutionStatus.OK;
	}

	private void handle(final ObdCommand command, final ConnectorResponse connectorResponse) {
		
		if (decoderThreadEnabled) {
			responseBuffer.addLast(new ConnectorResponseWrapper(command, connectorResponse));
		} else {
			final Collection<PidDefinition> variants = Context.instance().resolve(PidDefinitionRegistry.class).get()
					.findAllBy(command.getPid());
			if (variants.size() == 1) {
				validateAndPublish(buildMetric(command, connectorResponse));
			} else {
				variants.forEach(pid -> {
					validateAndPublish(buildMetric(new ObdCommand(pid), connectorResponse));
				});
			}
		}
		
	}
	
	private ObdMetric buildMetric(final ObdCommand command, final ConnectorResponse connectorResponse) {
		ObdMetricBuilder<?, ?> metricBuilder = ObdMetric.builder().command(command)
				.value(decode(command.getPid(), connectorResponse));

	
		metricBuilder = metricBuilder.raw(EMPTY_CONNECTOR_RESPONSE);
		return metricBuilder.build();
	}

	private Number decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		return (Number) Context.instance().resolve(CodecRegistry.class).get().findCodec(pid).decode(pid, connectorResponse);
	}

	@SuppressWarnings("unchecked")
	private void validateAndPublish(final ObdMetric metric) {
		Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
			final MetricValidator metricValidator = new MetricValidator();
			if (metricValidator.validate(metric) == MetricValidatorStatus.OK) {
				p.onNext(metric);
			}
		});
	}
}
