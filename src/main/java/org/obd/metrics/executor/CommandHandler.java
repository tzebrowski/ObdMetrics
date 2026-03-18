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

import org.obd.metrics.api.CommandProducer;
import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.Lifecycle.Subscription;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.Connector;

@FunctionalInterface
public interface CommandHandler {

	CommandExecutionStatus execute(Connector connector, Command command) throws Exception;

	static CommandHandler of(CommandsBuffer commandsBuffer, CommandProducer commandProducer,
			PidDefinitionRegistry pidRegistry, ConnectorResponseBuffer responseBuffer,
			EventsPublishlisher<Reply<?>> eventsPublishlisher, Subscription subscription) {
		return new DelegatingCommandHandler(commandsBuffer, commandProducer, pidRegistry, responseBuffer,
				eventsPublishlisher, subscription);
	}
}