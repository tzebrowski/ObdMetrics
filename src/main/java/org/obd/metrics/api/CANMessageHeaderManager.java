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
import org.obd.metrics.context.Context;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class CANMessageHeaderManager {

	private static final String AT_COMMAND = "AT";
	private final Map<String, String> canHeaders = new HashMap<String, String>();
	private final AtomicBoolean singleModeTest = new AtomicBoolean(false);
	private final AtomicBoolean addedSingleModeHeaderTest = new AtomicBoolean(false);
	private boolean isSingleMode = false;
	private String currentMode;
	private final CommandsBuffer buffer;

	CANMessageHeaderManager(Init init) {

		init.getHeaders().forEach(h -> {
			if (h.getMode() != null && h.getHeader() != null) {
				log.info("Found CAN header={} for mode={}", h.getHeader(), h.getMode());
				canHeaders.put(h.getMode(), h.getHeader());
			}
		});
		buffer = Context.instance().forceResolve(CommandsBuffer.class);
	}

	<T extends Command> void testSingleMode(List<T> commands) {
		if (singleModeTest.compareAndSet(false, true)) {
			final Set<String> groupedByMode = new HashSet<String>();
			commands.forEach(p -> {
				if (p.getCanMode() != null && p.getCanMode().length() > 0) {
					groupedByMode.add(p.getCanMode());
				}

				if (p.getMode() != null && p.getMode().length() > 0) {
					groupedByMode.add(p.getMode());
				}
			});

			if (groupedByMode.size() == 1) {
				isSingleMode = true;
			}

			log.info("Determined single mode={}, available modes={}", isSingleMode, groupedByMode);
		}
	}

	void switchHeader(Command nextCommand) {
		String nextMode = nextCommand.getCanMode();
		if (nextMode.length() == 0) {
			nextMode = nextCommand.getMode();
		}
		if (nextMode.equals(AT_COMMAND)) {
			return;
		}

		if (nextMode.equals(currentMode)) {
			if (log.isTraceEnabled()) {
				log.trace("Do not change CAN message header, previous header is the same. "
						+ "Current mode={}, next mode={}", currentMode, nextMode);
			}
		} else {
			currentMode = nextMode;
			final String nextHeader = canHeaders.get(nextMode);

			if (log.isTraceEnabled()) {
				log.trace("Setting CAN message header={} for the mode={}", nextHeader, nextMode);
			}

			if (canHeaders.containsKey(nextMode)) {
				if (isSingleMode) {
					if (addedSingleModeHeaderTest.compareAndSet(false, true)) {
						log.info("Injecting CAN message header={} for the mode to={}", nextHeader, nextMode);
						buffer.addLast(prepareCANMessageHeader(nextHeader));
					}
				} else {
					buffer.addLast(prepareCANMessageHeader(nextHeader));
				}
			}
		}
	}

	private ATCommand prepareCANMessageHeader(final String nextHeader) {
		return new ATCommand("SH" + nextHeader);
	}
}