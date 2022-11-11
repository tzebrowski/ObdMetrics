package org.obd.metrics.executor;

import java.util.Collection;

import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ObdMetric.ObdMetricBuilder;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.codec.Codec;
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
final class ObdCommandExecutor implements CommandExecutor {
	private final Adjustments adjustments;

	private static final ConnectorResponse EMPTY_CONNECTOR_RESPONSE = ConnectorResponseFactory.wrap(new byte[0]);

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
		final PidDefinitionRegistry pids = Context.instance().resolve(PidDefinitionRegistry.class).get();

		final Collection<PidDefinition> allVariants = pids.findAllBy(command.getPid());
		if (allVariants.size() == 1) {
			final ObdMetric metric = ObdMetric.builder().command(command)
					.value(decode(command.getPid(), connectorResponse)).raw(connectorResponse).build();
			validateAndPublish(metric);

		} else {
			allVariants.forEach(pid -> {
				ObdMetricBuilder<?, ?> value = ObdMetric.builder().command(new ObdCommand(pid))
						.value(decode(pid, connectorResponse));

				if (adjustments.isCollectRawConnectorResponseEnabled()) {
					value = value.raw(connectorResponse);
				} else {
					value = value.raw(EMPTY_CONNECTOR_RESPONSE);
				}

				validateAndPublish(value.build());
			});
		}
	}

	private Object decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		final CodecRegistry codecRegistry = Context.instance().resolve(CodecRegistry.class).get();

		final Codec<?> codec = codecRegistry.findCodec(pid);

		Object value = null;
		if (codec != null) {
			value = codec.decode(pid, connectorResponse);
		}
		return value;
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
