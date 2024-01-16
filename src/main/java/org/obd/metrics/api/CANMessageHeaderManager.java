/** 
 * Copyright 2019-2024, Tomasz Żebrowski
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

	private static final String AT_SET_HEADER = "SH";
	private final Map<String, String> serviceToCanHeadersMapping = new HashMap<String, String>();
	private final AtomicBoolean singleModeTest = new AtomicBoolean(false);
	private final AtomicBoolean addedSingleModeHeaderTest = new AtomicBoolean(false);
	private boolean isSingleService = false;
	private transient String currentServiceMapping;
	private final CommandsBuffer buffer;

	CANMessageHeaderManager(final Init init) {

		init.getHeaders().forEach(h -> {
			if (h.getService() != null && h.getValue() != null) {
				log.info("Found CAN header={} for service mapping={}", h.getValue(), h.getService());
				serviceToCanHeadersMapping.put(h.getService(), h.getValue());
			}
		});
		buffer = Context.instance().forceResolve(CommandsBuffer.class);
	}

	<T extends Command> void testIfSingleService(final List<T> commands) {
		if (singleModeTest.compareAndSet(false, true)) {
			final Set<String> groupedByService = new HashSet<String>();
			commands.forEach(p -> {
				if (p.getService() != null && p.getService().length() > 0) {
					groupedByService.add(p.getService());
				}
				if (p.getServiceOverrides() != null && p.getServiceOverrides().length() > 0) {
					groupedByService.add(p.getServiceOverrides());
				}
			});

			if (groupedByService.size() == 1) {
				isSingleService = true;
			}

			log.info("Determined single service={}, available services={}", isSingleService, groupedByService);
		}
	}

	void switchHeader(final Command nextCommand) {
		String nextServiceMapping = nextCommand.getServiceOverrides();
		if (nextServiceMapping.length() == 0) {
			nextServiceMapping = nextCommand.getService();
		}
		if (nextServiceMapping.equals(ATCommand.CODE)) {
			return;
		}

		if (nextServiceMapping.equals(currentServiceMapping)) {
			if (log.isTraceEnabled()) {
				log.trace("Do not change CAN message header, previous header is the same. "
						+ "Current service={}, next service={}", currentServiceMapping, nextServiceMapping);
			}
		} else {
			currentServiceMapping = nextServiceMapping;
			final String nextHeader = serviceToCanHeadersMapping.get(nextServiceMapping);

			if (log.isTraceEnabled()) {
				log.trace("Setting CAN message header={} for the mode to={}", nextHeader, nextServiceMapping);
			}

			if (serviceToCanHeadersMapping.containsKey(nextServiceMapping)) {
				if (isSingleService) {
					if (addedSingleModeHeaderTest.compareAndSet(false, true)) {
						log.info("Injecting CAN message header={} for the mode to {}", nextHeader, nextServiceMapping);
						buffer.addLast(prepareCANMessageHeader(nextHeader));
					}
				} else {
					buffer.addLast(prepareCANMessageHeader(nextHeader));
				}
			}
		}
	}

	private ATCommand prepareCANMessageHeader(final String nextHeader) {
		return new ATCommand(AT_SET_HEADER + nextHeader);
	}
}