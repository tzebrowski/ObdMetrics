package org.obd.metrics.api;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ObdMetric.ObdMetricBuilder;
import org.obd.metrics.buffer.decoder.ConnectorResponseWrapper;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.executor.MetricValidator;
import org.obd.metrics.executor.MetricValidator.MetricValidatorStatus;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
final class CommandDecoder extends LifecycleAdapter implements Callable<Void> {

	private final Adjustments adjustments;
	private static final ConnectorResponse EMPTY_CONNECTOR_RESPONSE = ConnectorResponseFactory.empty();

	@Override
	public Void call() throws Exception {

		try {
			final ConnectorResponseBuffer buffer = Context.instance().resolve(ConnectorResponseBuffer.class).get();

			while (!isStopped) {
				if (isRunning) {
					final ConnectorResponseWrapper response = buffer.get();
					if (response == null) {
						continue;
					}

					handle(response);
				} else {
					log.trace("No commands are provided by supplier yet");
				}
			}
		} catch (InterruptedException e) {
			log.info("Process was interupted.");
		} finally {
			log.info("Completed decoder thread.");
		}
		return null;
	}

	private void handle(ConnectorResponseWrapper response) {
		final ObdCommand command = response.getCommand();
		final ConnectorResponse connectorResponse = response.getConnectorResponse();

		long tt = System.currentTimeMillis();
		final Collection<PidDefinition> variants = Context.instance().resolve(PidDefinitionRegistry.class).get()
				.findAllBy(command.getPid());
		if (variants.size() == 1) {
			validateAndPublish(buildMetric(command, connectorResponse));
		} else {
			variants.forEach(pid -> {
				validateAndPublish(buildMetric(new ObdCommand(pid), connectorResponse));
			});
		}

		if (log.isTraceEnabled()) {
			tt = System.currentTimeMillis() - tt;
			log.trace("processing time {}ms", tt);
		}
	}

	private ObdMetric buildMetric(final ObdCommand command, final ConnectorResponse connectorResponse) {
		ObdMetricBuilder<?, ?> metricBuilder = ObdMetric.builder().command(command)
				.value(decode(command.getPid(), connectorResponse));

		if (adjustments.isCollectRawConnectorResponseEnabled()) {
			metricBuilder = metricBuilder.raw(connectorResponse);
		} else {
			metricBuilder = metricBuilder.raw(EMPTY_CONNECTOR_RESPONSE);
		}
		return metricBuilder.build();
	}

	private Number decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		return (Number) Context.instance().resolve(CodecRegistry.class).get().findCodec(pid).decode(pid,
				connectorResponse);
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
