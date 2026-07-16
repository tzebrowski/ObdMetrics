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

import org.obd.metrics.api.model.Init;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.Command;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CANNetworkMultiplexer {

	private final CommandsBuffer commandsBuffer;
	private final CANNetworkRegistry registry;
	private CANNetwork currentNetwork = null;

	CANNetworkMultiplexer(final Init init, final CommandsBuffer commandsBuffer) {
		this.commandsBuffer = commandsBuffer;
		this.registry = init.getNetworkRegistry();
	}

	void switchNetwork(Command nextCommand) {
		try {
			if (!(nextCommand instanceof ObdCommand)) {
				return;
			}

			
			final CANNetwork nextNetwork = ((ObdCommand) nextCommand).getCanNetwork();

			if (null == nextNetwork) {
				return;
			}
			
			if (nextNetwork == currentNetwork) {
				return;
			}

			if (log.isDebugEnabled()) {
				log.debug("Switching CAN network boundaries from {} to {}", currentNetwork, nextNetwork);
			}

			currentNetwork = nextNetwork;
			final List<Command> switchSequence = registry.getSwitchCommands(nextNetwork);
			
			if (switchSequence.isEmpty()) {
				if (log.isTraceEnabled()) {
					log.trace("No custom network switch macro registered for network: {}", nextNetwork);
				}
			} else {
				switchSequence.forEach(commandsBuffer::addLast);
			}
		} catch (Throwable e) {
			log.error("Failed to switch CAN Network", e);
		}
	}
}