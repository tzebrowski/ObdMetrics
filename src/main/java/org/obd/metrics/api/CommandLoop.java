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

import java.io.IOException;
import java.util.concurrent.Callable;

import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.context.Context;
import org.obd.metrics.executor.CommandExecutionStatus;
import org.obd.metrics.executor.CommandHandler;
import org.obd.metrics.transport.Connector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CommandLoop extends LifecycleAdapter implements Callable<Void> {

	private static final int SLEEP_BETWEEN_COMMAND_EXECUTION = 2;
	private volatile boolean isStopped = false;

	@Override
	public Void call() throws Exception {

		log.info("Starting command executor thread..");
		final Context context = Context.instance();
		final CommandsBuffer buffer = context.forceResolve(CommandsBuffer.class);
		final CommandHandler handler = CommandHandler.of();

		try (final ConnectionManager connectionManager = context.forceResolve(ConnectionManager.class)) {

			while (!isStopped) {
				Thread.sleep(SLEEP_BETWEEN_COMMAND_EXECUTION);
				try {
					final Connector connector = connectionManager.getConnector();
					if (connector == null) {
						Thread.sleep(SLEEP_BETWEEN_COMMAND_EXECUTION);	
					} else {
						if (connector.isFaulty()) {
							Subscription.notifyOnInternalError("Device connection is faulty.", null);
						} else {
							
							final Command command = buffer.get();
							final CommandExecutionStatus status = handler.execute(connector, command);
							if (CommandExecutionStatus.ABORT.equals(status)) {
								return null;
							} else if (CommandExecutionStatus.OK.equals(status)) {
								connectionManager.resetFaultCounter();
								continue;
							} else {
								Subscription.notifyOnInternalError(status.getMessage().name());
							}
						}
					}
				} catch (IOException e) {
					Subscription.notifyOnInternalError("IO Exception occured: " + e.getMessage(), e);
				}
			}

		} catch (InterruptedException e) {
			log.info("Commmand Loop is interupted");
		} catch (Throwable e) {
			log.info("Commmand Loop Failed", e);
			Subscription.notifyOnInternalError(String.format("Command Loop failed: %s", e.getMessage()));
		} finally {
			log.info("Completed Commmand Loop.");
		}
		return null;
	}
}