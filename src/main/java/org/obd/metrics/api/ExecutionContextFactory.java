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

import java.util.Arrays;
import java.util.List;

import org.obd.metrics.alert.Alerts;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.api.model.SniffingPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.diagnostic.Diagnostics;
import org.obd.metrics.executor.CommandHandler;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class ExecutionContextFactory {
    
	private final PidDefinitionRegistry registry;
    private final FormulaEvaluatorConfig formulaEvaluatorConfig;
    private final ReplyObserver<Reply<?>> externalEventsObserver;
    private final List<Lifecycle> lifecycle;
    
    ExecutionContext build(AdapterConnection conn, Init init, Adjustments adj, Query query, SniffingPolicy sniffing) {
        
    	final Subscription subscription = new Subscription();
        final CommandsBuffer cb = CommandsBuffer.instance();
        final ConnectorResponseBuffer rb = ConnectorResponseBuffer.instance();
    	final Diagnostics diagnostics = Diagnostics.instance();
    	final Alerts alerts = Alerts.instance();

        @SuppressWarnings("unchecked")
		final EventsPublishlisher<Reply<?>> publisher = EventsPublishlisher.builder()
                .observer(new RoutinesResponseObserver<>(subscription))
                .observer(externalEventsObserver)
                .observer((ReplyObserver<Reply<?>>) alerts)
                .observer((ReplyObserver<Reply<?>>) diagnostics).build();

        final ConnectionManager connectionManager = new ConnectionManager(conn, adj, subscription, publisher, cb);
        lifecycle.forEach(subscription::subscribe);

        final CodecRegistry codecRegistry = CodecRegistry.builder()
                .registry(registry)
                .formulaEvaluatorConfig(formulaEvaluatorConfig)
                .subscription(subscription)
                .adjustments(adj)
                .build();

        Query effectiveQuery = query;
        if (sniffing != null && sniffing.isEnabled()) {
            effectiveQuery = Query.builder().pid( SniffingSupport.pid(sniffing).getId()).build();
        }

        final CommandsSuplier commandsSupplier = new CommandsSuplier(registry, adj, effectiveQuery, init);
	    final CommandProducer producer = new CommandProducer(diagnostics, 
                commandsSupplier, adj, init, cb);
        
        final CommandHandler handler = CommandHandler.of(cb, producer, registry, rb, publisher, subscription);
        final CommandLoop loop = new CommandLoop(cb, connectionManager, subscription, handler);
        final ConnectorResponseDecoder decoder = new ConnectorResponseDecoder(rb, adj, registry, codecRegistry, publisher);

        subscription.subscribe(decoder);
        subscription.subscribe(producer);
        subscription.subscribe(loop);
        subscription.subscribe(connectionManager);
        subscription.onConnecting();
	
        return ExecutionContext.builder()
                .connectionManager(connectionManager)
                .commandProducer(producer)
                .subscription(subscription)
                .commandsBuffer(cb)
                .responseBuffer(rb)
                .diagnostics(diagnostics)
                .alerts(alerts)
                .workerThreads(Arrays.asList(loop, producer, decoder)).build();
    }
}