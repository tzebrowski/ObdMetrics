 /**
 * Copyright 2019-2025, Tomasz Żebrowski
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
	private final MetricValidator metricValidator = new MetricValidator();

	@Override
	public Void call() throws Exception {

		try {
			final ConnectorResponseBuffer buffer = Context.instance().forceResolve(ConnectorResponseBuffer.class);

			while (!isStopped) {

				final ConnectorResponseWrapper response = buffer.get();

				if (response == null) {
					continue;
				}

				handle(response);

			}
		} catch (InterruptedException e) {
			log.info("Decoder thread was interupted.");
		} catch (Throwable e) {
			log.error("Unexpected error happended.", e);
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
			decodeAndPublish(command, connectorResponse);
		} else {
			variants.forEach(pid -> {
				decodeAndPublish(new ObdCommand(pid), connectorResponse);
			});
		}

		if (log.isTraceEnabled()) {
			tt = System.currentTimeMillis() - tt;
			log.trace("processing time {}ms", tt);
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
			return Context.instance().forceResolve(CodecRegistry.class).findCodec(pid).decode(pid, connectorResponse);
		} catch (Throwable e) {
			log.error("Failed to decoder the message", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private void decodeAndPublish(final ObdCommand command, final ConnectorResponse connectorResponse) {

		if (log.isTraceEnabled()) {
			log.trace("Pid:{}, value:{}", command.getPid().getId(), connectorResponse.getMessage());
		}

		Object value = decode(command.getPid(), connectorResponse);
		if (value instanceof Number) {
			final Number numberValue = (Number) value;
			final MetricValidatorStatus validationResult = metricValidator.validate(command.getPid(), numberValue);

			final boolean inAlert = (validationResult == MetricValidatorStatus.IN_ALERT_UPPER
					|| validationResult == MetricValidatorStatus.IN_ALERT_LOWER);

			if (validationResult == MetricValidatorStatus.OK || inAlert) {
				Context.instance().forceResolve(EventsPublishlisher.class)
						.onNext(buildMetric(command, connectorResponse, numberValue,
								validationResult == MetricValidatorStatus.IN_ALERT_UPPER,
								validationResult == MetricValidatorStatus.IN_ALERT_LOWER));
			}
		} else if (value != null) {

			Context.instance().forceResolve(EventsPublishlisher.class)
					.onNext(buildMetric(command, connectorResponse, value, false, false));
		} else {
			//
		}
	}
}
