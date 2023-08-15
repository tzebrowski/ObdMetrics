package org.obd.metrics.api;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ObdMetric.ObdMetricBuilder;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseWrapper;
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
public final class ConnectorResponseDecoder extends LifecycleAdapter implements Callable<Void> {

	private final Adjustments adjustments;
	private static final ConnectorResponse EMPTY_CONNECTOR_RESPONSE = ConnectorResponseFactory.empty();

	@Override
	public Void call() throws Exception {

		try {
			final ConnectorResponseBuffer buffer = Context.instance().forceResolve(ConnectorResponseBuffer.class);

			while (!isStopped) {
				if (isRunning) {
					final ConnectorResponseWrapper response = buffer.get();
					if (response == null) {
						continue;
					}

					handle(response);
				} else {
					if (log.isTraceEnabled()) {
						log.trace("No commands are provided by supplier yet");
					}
				}
			}
		} catch (InterruptedException e) {
			log.info("Decoder thread was interupted.");
		} finally {
			log.info("Completed decoder thread.");
		}
		return null;
	}

	private void handle(ConnectorResponseWrapper response) {
		final ObdCommand command = response.getCommand();
		final ConnectorResponse connectorResponse = response.getConnectorResponse();

		long tt = System.currentTimeMillis();
		final Collection<PidDefinition> variants = Context.instance().forceResolve(PidDefinitionRegistry.class)
				.findAllBy(command.getPid());
		if (variants.size() == 1) {
			validateAndPublish(command, connectorResponse);
		} else {
			variants.forEach(pid -> {
				validateAndPublish(new ObdCommand(pid), connectorResponse);
			});
		}

		if (log.isTraceEnabled()) {
			tt = System.currentTimeMillis() - tt;
			log.trace("processing time {}ms", tt);
		}
	}

	private ObdMetric buildMetric(final ObdCommand command, 
			final ConnectorResponse connectorResponse, 
			final Number value, boolean inAlert) {
		
		ObdMetricBuilder<?, ?> metricBuilder = ObdMetric.builder().command(command)
				.value(value).alert(inAlert);

		if (adjustments.isCollectRawConnectorResponseEnabled()) {
			metricBuilder = metricBuilder.raw(connectorResponse);
		} else {
			metricBuilder = metricBuilder.raw(EMPTY_CONNECTOR_RESPONSE);
		}
		return metricBuilder.build();
	}

	private Number decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		return (Number) Context.instance().forceResolve(CodecRegistry.class).findCodec(pid).decode(pid,
				connectorResponse);
	}

	@SuppressWarnings("unchecked")
	private void validateAndPublish(final ObdCommand command, final ConnectorResponse connectorResponse) {
		final EventsPublishlisher<Reply<?>> eventsPublishlisher = Context.instance().forceResolve(EventsPublishlisher.class);
		final Number value = decode(command.getPid(), connectorResponse);
		
		final MetricValidator metricValidator = new MetricValidator();
		final MetricValidatorStatus validationResult = metricValidator.validate(command.getPid(),value);
		if (validationResult == MetricValidatorStatus.OK || 
				validationResult == MetricValidatorStatus.IN_ALERT) {
			eventsPublishlisher.onNext(buildMetric(command, 
					connectorResponse, value, 
					validationResult == MetricValidatorStatus.IN_ALERT));
		} 
	}
}
