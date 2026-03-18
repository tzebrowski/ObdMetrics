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

import org.obd.metrics.api.EventsPublishlisher;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class QuitCommandHandler implements CommandHandler {
	private final EventsPublishlisher<Reply<?>> eventsPublishlisher;

	QuitCommandHandler(EventsPublishlisher<Reply<?>> eventsPublishlisher) {
		this.eventsPublishlisher = eventsPublishlisher;
	}

	@Override
	public CommandExecutionStatus execute(Connector connector, Command command) throws InterruptedException {

		log.info("Stopping Command Loop thread. Finishing communication.");
		eventsPublishlisher.onNext(Reply.builder().command(new QuitCommand()).build());
		eventsPublishlisher.onCompleted();

		return CommandExecutionStatus.ABORT;
	}
}
