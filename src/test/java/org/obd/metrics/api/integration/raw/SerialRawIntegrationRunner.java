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
package org.obd.metrics.api.integration.raw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.obd.metrics.api.CommandLoop;
import org.obd.metrics.api.ConnectionManager;
import org.obd.metrics.api.ConnectorResponseDecoder;
import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.Resources;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.connection.SerialConnection;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SerialRawIntegrationRunner {
	
	protected void runSerialTest(final String portName, final Pids pids, final CommandsBuffer buffer, final Adjustments optional)
			throws IOException, InterruptedException {

		final PidDefinitionRegistry registry = toPidRegistry(pids);
		final CodecRegistry codecRegistry = CodecRegistry.builder()
				.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().build()).adjustments(optional).build();
		
		final AdapterConnection connection = SerialConnection.of(portName);
		final EventsPublishlisher<Reply<?>> eventsPublisher = EventsPublishlisher.builder().build();
		final Subscription subscription = new Subscription();
		final ConnectionManager connectionManager = new ConnectionManager(connection, optional,subscription, eventsPublisher, buffer);
		
		final ConnectorResponseBuffer connectorResponseBuffer = ConnectorResponseBuffer.instance();
		final Callable<Void> decoder = new ConnectorResponseDecoder(connectorResponseBuffer, optional, registry, codecRegistry, eventsPublisher);
		
		
		final Callable<Void> loop = new CommandLoop(buffer, connectionManager, subscription,
				null, registry, connectorResponseBuffer, eventsPublisher);


		
		subscription.subscribe((org.obd.metrics.api.model.Lifecycle) decoder);
		subscription.subscribe((org.obd.metrics.api.model.Lifecycle) connectionManager);
		subscription.subscribe((org.obd.metrics.api.model.Lifecycle) loop);
		subscription.onConnecting();
	
		
		final ExecutorService executorService = Executors.newFixedThreadPool(3);
		List<Callable<Void>> threadsList = new ArrayList<Callable<Void>>();
		threadsList.add(loop);
		threadsList.add(decoder);
		
		threadsList.add( () ->{

			while (true) {
				Thread.sleep(10);
				if (buffer.size() == 0) {
					log.info("No commands in the queue. Finalizing.");
					executorService.shutdownNow();
					return null;
				}
			}
		
		});
		executorService.invokeAll(threadsList);
	}

	protected PidDefinitionRegistry toPidRegistry(Pids pids) {
		PidDefinitionRegistry pidRegistry = null;
		try (final Resources sources = Resources.convert(pids)) {
			pidRegistry = PidDefinitionRegistry.builder().sources(sources.getResources()).build();
		}
		return pidRegistry;
	}
}
