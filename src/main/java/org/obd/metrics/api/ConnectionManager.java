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

import java.util.List;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.Connector;
import org.obd.metrics.transport.message.AdapterErrorType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class ConnectionManager extends LifecycleAdapter implements AutoCloseable {
	private final AdapterConnection connection;
	private final Adjustments adjustments;
	private final Subscription subscription;
	private final EventsPublishlisher<Reply<?>> eventsPublishlisher;
	private final CommandsBuffer commandsBuffer;

	@Getter
	private Connector connector;
	
	private volatile int numberOfReconnectRetries = 0;

	void update(List<ObdCommand> commands) {
		try {
			if (connection != null) {
				log.info("Updating AdapterConnection with new commands.");
				connection.update(commands);
			}
		} catch (Throwable e) {
			log.error("Failed to update connection", e);
		}
	}
	
	@Override
	public void onInternalError(String reason, Throwable e) {
		final boolean isReconnectAllowed = isReconnectAllowed();

		log.error(
				"Received onInternalError event, reconnect.enabled={}, reconnect.counter={},"
						+ " number.of.retries={}, allowed.to.reconnect={}, reason: {}",
				adjustments.getErrorsPolicy().isReconnectEnabled(), numberOfReconnectRetries,
				adjustments.getErrorsPolicy().getNumberOfRetries(), isReconnectAllowed, reason, e);

		if (adjustments.getErrorsPolicy().isReconnectEnabled() && isReconnectAllowed) {
			reconnect(reason);
		} else {
			log.error("Raising Subscription.onError={}", reason);
			subscription.onError(reason, e);
			eventsPublishlisher.onError(new Exception(reason));
			eventsPublishlisher.onNext(Reply.builder().command(new QuitCommand()).build());
		}
	}
	
	
	@SneakyThrows
	@Override
	public void onConnecting() {
		this.connector = Connector.builder().adjustments(adjustments).connection(connection).build();
	}


	@SneakyThrows
	@Override
	public void close() {
		if (connector != null) {
			connector.close();
		}
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

	private void reconnect(String reason) {
		try {

			++numberOfReconnectRetries;

			final AdapterErrorType adapterErrorType = AdapterErrorType.map(reason);

			if (AdapterErrorType.NO_DATA.equals(adapterErrorType)
					|| AdapterErrorType.STOPPED.equals(adapterErrorType)) {

				log.info("Doing soft recovery, reason={}", reason);
				commandsBuffer.clear();
				commandsBuffer.add(DefaultCommandGroup.RECOVERY_AFTER_STOPPED);
				log.info("Soft recovery sequence added to buffer");

			} else if (AdapterErrorType.BUSBUSY.equals(adapterErrorType)) {

				log.info("Doing soft recovery for BUSBUSY");
				commandsBuffer.addFirst(new DelayCommand(200));
				log.info("Soft recovery sequence added to buffer");

			} else if (AdapterErrorType.CANERROR.equals(adapterErrorType)) {
				log.info("Doing soft recovery for CANERROR");
				commandsBuffer.clear();
				commandsBuffer.add(DefaultCommandGroup.CAN_ERROR_RESET);
				commandsBuffer.add(DefaultCommandGroup.INIT);
				log.info("Soft recovery sequence added to buffer");
			} else {
				log.error("Connector is faulty, reason={}. Resetting current connection: {}", reason, connection);
				connector.close();
				connector = Connector.builder().connection(connection).build();
			}

		} catch (Throwable e) {
			log.error("Failed to reconnect. ", e);
			subscription.onInternalError("Failed to reconnect. ", e);
		}
	}
}