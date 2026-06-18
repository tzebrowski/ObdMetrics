 /**
 * Copyright 2019-2026, Tomasz Żebrowski
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.obd.metrics.api;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ObdMetric.ObdMetricBuilder;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.SnifferMetric;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseWrapper;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.executor.MetricValidator;
import org.obd.metrics.executor.MetricValidator.MetricValidatorStatus;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class ConnectorResponseDecoder extends LifecycleAdapter implements Callable<Void> {
	
	private final ConnectorResponseBuffer buffer;
	private final Adjustments adjustments;
	private static final ConnectorResponse EMPTY_CONNECTOR_RESPONSE = ConnectorResponseFactory.EMPTY_CONNECTOR_RESPONSE;
	private final MetricValidator metricValidator = new MetricValidator();
	private final PidDefinitionRegistry registry;
	private final CodecRegistry codecRegistry;
	private final EventsPublishlisher<Reply<?>> eventsPublisher;
	
	@Override
	public Void call() throws Exception {

		try {
			while (!isStopped && !Thread.currentThread().isInterrupted()) {

				final ConnectorResponseWrapper response = buffer.get();

				if (response == null) {
					continue;
				}
				
				handle(response);
			}
		} catch (InterruptedException e) {
			isStopped = true;
			log.info("Decoder thread was interupted.");
			Thread.currentThread().interrupt();
		} catch (Throwable e) {
			isStopped = true;
			log.error("Unexpected error happended.", e);
		} finally {
			isStopped = true;
			log.info("Completed decoder thread.");
		}
		return null;
	}

	private void handle(ConnectorResponseWrapper response) {
		final ObdCommand command = response.getCommand();
		final ConnectorResponse connectorResponse = response.getConnectorResponse();
		
		long timeTaken = System.currentTimeMillis();
		final Collection<PidDefinition> variants = registry.findAllBy(command.getPid());
		
		if (variants.size() == 1 || ( adjustments.getSniffing() != null && adjustments.getSniffing().isEnabled())) {
			decodeAndPublish(command, connectorResponse);
		} else {
			variants.forEach(pid -> {
				decodeAndPublish(new ObdCommand(pid), connectorResponse);
			});
		}

		if (log.isTraceEnabled()) {
			timeTaken = System.currentTimeMillis() - timeTaken;
			log.trace("processing time {}ms", timeTaken);
		}
	}

	private ObdMetric buildMetric(final ObdCommand command, final ConnectorResponse connectorResponse,
			final Object value, boolean upperAlert, boolean lowerAlert) {

		ObdMetricBuilder<?, ?> metricBuilder = ObdMetric.builder().command(command).value(value).upperAlert(upperAlert)
				.lowerAlert(lowerAlert);

		if (adjustments.isCollectRawConnectorResponseEnabled()) {
			metricBuilder = metricBuilder.raw(connectorResponse);
		} else {
			metricBuilder = metricBuilder.raw(EMPTY_CONNECTOR_RESPONSE);
		}
		return metricBuilder.build();
	}

	private Object decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		try {
			return codecRegistry.findCodec(pid).decode(pid, connectorResponse);
		} catch (Throwable e) {
			log.error("Failed to decoder the message", e);
			return null;
		}
	}

	private void decodeAndPublish(final ObdCommand command, final ConnectorResponse connectorResponse) {

		if (log.isTraceEnabled()) {
			log.trace("Pid:{}, value:{}", command.getPid().getId(), connectorResponse.getMessage());
		}
		if (null != adjustments.getSniffing() && adjustments.getSniffing().isEnabled()) {
			eventsPublisher.onNext(SnifferMetric.builder().command(command).raw(connectorResponse).build());
		}else {
			final Object value = decode(command.getPid(), connectorResponse);
			if (value instanceof Number) {
				final Number numberValue = (Number) value;
				final MetricValidatorStatus validationResult = metricValidator.validate(command.getPid(), numberValue);
	
				final boolean inAlert = (validationResult == MetricValidatorStatus.IN_ALERT_UPPER
						|| validationResult == MetricValidatorStatus.IN_ALERT_LOWER);
	
				if (validationResult == MetricValidatorStatus.OK || inAlert) {
					eventsPublisher
							.onNext(buildMetric(command, connectorResponse, numberValue,
									validationResult == MetricValidatorStatus.IN_ALERT_UPPER,
									validationResult == MetricValidatorStatus.IN_ALERT_LOWER));
				}
			} else if (value != null) {
				eventsPublisher.onNext(buildMetric(command, connectorResponse, value, false, false));
			} else {
				//
			}
		}
	}
}
