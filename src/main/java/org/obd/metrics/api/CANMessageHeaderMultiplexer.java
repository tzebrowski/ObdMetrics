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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.obd.metrics.api.model.Init;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.Command;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CANMessageHeaderMultiplexer {

	private static final String AT_COMMAND = "AT";

	private final Map<String, ATCommand> canHeaders = new HashMap<>();

	private final AtomicBoolean singleModeTest = new AtomicBoolean(false);
	private final AtomicBoolean addedSingleModeHeaderTest = new AtomicBoolean(false);

	private volatile boolean isSingleMode = false;
	private volatile String currentMode;
	private volatile CANNetwork currentNetwork;

	private final CommandsBuffer commandsBuffer;

	CANMessageHeaderMultiplexer(final Init init, final CommandsBuffer commandsBuffer) {
		this.commandsBuffer = commandsBuffer;

		init.getHeaders().forEach(h -> {
			if (h.getMode() != null && h.getHeader() != null) {
				log.info("Found CAN header={} for mode={}", h.getHeader(), h.getMode());
				canHeaders.put(h.getMode(), new ATCommand("SH" + h.getHeader()));
			}
		});
	}

	<T extends Command> void testSingleMode(List<T> commands) {
		if (commands == null || commands.isEmpty()) {
			return;
		}

		if (singleModeTest.compareAndSet(false, true)) {
			final Set<String> groupedByMode = new HashSet<>();

			commands.forEach(p -> {
				if (p != null) {
					final String canMode = p.getCanMode();
					if (canMode != null && !canMode.isEmpty()) {
						groupedByMode.add(canMode);
					}

					final String mode = p.getMode();
					if (mode != null && !mode.isEmpty()) {
						groupedByMode.add(mode);
					}
				}
			});

			if (groupedByMode.size() == 1) {
				isSingleMode = true;
			}

			log.info("Determined single mode={}, available modes={}", isSingleMode, groupedByMode);
		}
	}

	void switchHeader(Command nextCommand) {
		if (nextCommand == null) {
			return;
		}

		String nextMode = nextCommand.getCanMode();
		if (nextMode == null || nextMode.isEmpty()) {
			nextMode = nextCommand.getMode();
		}

		if (nextMode == null || AT_COMMAND.equals(nextMode)) {
			return;
		}

		final CANNetwork nextNetwork = nextCommand.getCanNetwork();

		if (nextMode.equals(currentMode) && nextNetwork == currentNetwork) {
			if (log.isTraceEnabled()) {
				log.trace("Do not change CAN message header, previous header is the same. "
						+ "Current mode={}, next mode={}", currentMode, nextMode);
			}

		} else {

			currentMode = nextMode;
			currentNetwork = nextNetwork;

			final ATCommand nextHeaderCommand = canHeaders.get(nextMode);

			if (nextHeaderCommand != null) {
				if (log.isTraceEnabled()) {
					log.trace("Setting CAN message header={} for the mode={}", nextHeaderCommand.getQuery(), nextMode);
				}

				if (isSingleMode) {
					if (addedSingleModeHeaderTest.compareAndSet(false, true)) {
						log.info("Injecting CAN message header={} for the mode to={}", nextHeaderCommand.getQuery(),
								nextMode);
						commandsBuffer.addLast(nextHeaderCommand);
					}
				} else {
					commandsBuffer.addLast(nextHeaderCommand);
				}
			}
		}
	}
}