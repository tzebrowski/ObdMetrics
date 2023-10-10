/** 
 * Copyright 2019-2023, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.api;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.context.Context;
import org.obd.metrics.context.Service;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.Connector;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class ConnectionManager extends LifecycleAdapter implements AutoCloseable, Service {
	private final AdapterConnection connection;
	private final Adjustments adjustments;

	private volatile int numberOfReconnectRetries = 0;

	@Override
	public void onInternalError(String message, Throwable e) {
		log.error("Received onInternalError event. Counter={}, reason: {}", numberOfReconnectRetries, message);

		if (adjustments.getErrorsPolicy().isReconnectEnabled()
				&& numberOfReconnectRetries < adjustments.getErrorsPolicy().getNumberOfRetries()) {
			reconnect();
		} else {
			Context.instance().resolve(Subscription.class).apply(p -> {
				p.onError(message, e);
			});

			Context.instance().resolve(EventsPublishlisher.class).apply(p -> {
				p.onError(new Exception(message));
				p.onNext(Reply.builder().command(new QuitCommand()).build());
			});
		}
	}

	@SneakyThrows
	@Override
	public void close() {
		getConnector().close();
	}

	@SneakyThrows
	@Override
	public void onInit() {
		Context.instance().register(Connector.class,
				Connector.builder().adjustments(adjustments).connection(connection).build());
	}

	Connector getConnector() {
		return Context.instance().forceResolve(Connector.class);
	}

	boolean isReconnectAllowed() {
		return adjustments.getErrorsPolicy().isReconnectEnabled()
				&& numberOfReconnectRetries < adjustments.getErrorsPolicy().getNumberOfRetries();
	}

	void resetFaultCounter() {
		if (log.isDebugEnabled()) {
			log.debug("Reseting error counter");
		}

		numberOfReconnectRetries = 0;
	}

	private void reconnect() {
		try {
			numberOfReconnectRetries++;
			final String msg = "Connector is faulty. Reconnecting.....";
			log.error(msg);

			final Context context = Context.instance();
			Connector connector = context.forceResolve(Connector.class);
			connector.close();
			connector = Connector.builder().connection(connection).build();
			context.register(Connector.class, connector);
		} catch (Throwable e) {
			log.error("Failed to reconnect. ", e);
			Subscription.notifyOnInternalError("Failed to reconnect. ");
		}
	}
}
