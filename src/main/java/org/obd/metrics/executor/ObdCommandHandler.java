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
package org.obd.metrics.executor;

import java.util.Map;

import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.ErrorsPolicy;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseWrapper;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.routine.RoutineCommand;
import org.obd.metrics.pool.ObjectAllocator;
import org.obd.metrics.transport.Connector;
import org.obd.metrics.transport.message.AdapterErrorType;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ObdCommandHandler implements CommandHandler {
	
	private final ConnectorResponseBuffer responseBuffer;
	private final EventsPublishlisher<Reply<?>> eventsPublishlisher;
	private final ErrorsPolicy errorPolicy;
	private final static ObjectAllocator<ConnectorResponseWrapper> allocator = ObjectAllocator
			.of(ObjectAllocator.Strategy.Circular, ConnectorResponseWrapper.class, 255);

	ObdCommandHandler(EventsPublishlisher<Reply<?>> eventsPublishlisher, ConnectorResponseBuffer responseBuffer, ErrorsPolicy errorPolicy) {
		this.responseBuffer = responseBuffer;
		this.eventsPublishlisher = eventsPublishlisher;
		this.errorPolicy = errorPolicy;
	}
	
	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) {
		connector.transmit(command);
		final ConnectorResponse connectorResponse = connector.receive();
		if (command instanceof RoutineCommand) {
			log.debug("Received routine commmand response");
			publishResponse(command, connectorResponse);
		} else {
			if (connectorResponse.isEmpty()) {
				log.debug("Received no data");

			} else if (connectorResponse.findError() != AdapterErrorType.NONE && errorPolicy.isContinueOnError()) {
				log.info("Received adapter error: {}. Continue on error", connectorResponse.getMessage());
						
			} else if (connectorResponse.findError() != AdapterErrorType.NONE && !errorPolicy.isContinueOnError()) {
				log.error("Received adapter error: {}", connectorResponse.getMessage());
				return new CommandExecutionStatus(connectorResponse.findError());
			
			} else if (command instanceof BatchObdCommand) {
				final BatchObdCommand batchCommand = (BatchObdCommand) command;

				final Map<ObdCommand, ConnectorResponse> batchDecoderResp = batchCommand.decode(connectorResponse);

				if (batchDecoderResp.isEmpty()) {
					final AdapterErrorType error = connectorResponse.findError(true);
					if (error != AdapterErrorType.NONE) {
						log.error("Received adapter error: {}", connectorResponse.getMessage());
						return new CommandExecutionStatus(error);
					}
				} else {
					batchDecoderResp.forEach(this::handle);
				}
			} else if (command instanceof ObdCommand) {
				handle((ObdCommand) command, connectorResponse);
			} else {
				publishResponse(command, connectorResponse);
			}
		}
		return CommandExecutionStatus.OK;
	}

	private void handle(final ObdCommand command, final ConnectorResponse connectorResponse) {
		
		final ConnectorResponseWrapper allocate = allocator.allocate();
		allocate.setCommand(command);
		allocate.setConnectorResponse(connectorResponse);
		responseBuffer.addLast(allocate);
	}

	private void publishResponse(Command command, final ConnectorResponse connectorResponse) {
		eventsPublishlisher.onNext(Reply.builder().command(command).raw(connectorResponse).build());
	}
}
