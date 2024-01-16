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
final class DiagnosticRequestIDManager {

	private static final String AT_SET_HEADER = "SH";
	private final Map<String, String> driMapping = new HashMap<String, String>();
	private final AtomicBoolean singleDriTest = new AtomicBoolean(false);
	private final AtomicBoolean addedSingleModeHeaderTest = new AtomicBoolean(false);
	private boolean isSingleService = false;
	private transient String currenteKeyMapping;
	private final CommandsBuffer buffer;

	DiagnosticRequestIDManager(final Init init) {

		init.getDiagnosticRequestIDMapping().forEach(h -> {
			if (h.getKey() != null && h.getValue() != null) {
				log.info("Found DRI value={} for service key={}", h.getValue(), h.getKey());
				driMapping.put(h.getKey(), h.getValue());
			}
		});
		buffer = Context.instance().forceResolve(CommandsBuffer.class);
	}

	<T extends Command> void testIfSingleService(final List<T> commands) {
		if (singleDriTest.compareAndSet(false, true)) {
			final Set<String> groupedByValues = new HashSet<String>();
			commands.forEach(p -> {
				if (p.getService() != null && p.getService().length() > 0) {
					groupedByValues.add(p.getService());
				}
				if (p.getServiceOverrides() != null && p.getServiceOverrides().length() > 0) {
					groupedByValues.add(p.getServiceOverrides());
				}
			});

			if (groupedByValues.size() == 1) {
				isSingleService = true;
			}

			log.info("Determined single value={}, available values={}", isSingleService, groupedByValues);
		}
	}

	void switchHeader(final Command nextCommand) {
		String nextKeyMapping = nextCommand.getServiceOverrides();
		if (nextKeyMapping.length() == 0) {
			nextKeyMapping = nextCommand.getService();
		}
		if (nextKeyMapping.equals(ATCommand.CODE)) {
			return;
		}

		if (nextKeyMapping.equals(currenteKeyMapping)) {
			if (log.isTraceEnabled()) {
				log.trace("Do not change DRI message value, previous DRI is the same. "
						+ "Current service={}, next service={}", currenteKeyMapping, nextKeyMapping);
			}
		} else {
			currenteKeyMapping = nextKeyMapping;
			final String nextValue = driMapping.get(nextKeyMapping);

			if (log.isTraceEnabled()) {
				log.trace("Setting DRI message value={} for the mode to={}", nextValue, nextKeyMapping);
			}

			if (driMapping.containsKey(nextKeyMapping)) {
				if (isSingleService) {
					if (addedSingleModeHeaderTest.compareAndSet(false, true)) {
						log.info("Injecting DRI message key={} for the value to {}", nextValue, nextKeyMapping);
						buffer.addLast(prepareDRI(nextValue));
					}
				} else {
					buffer.addLast(prepareDRI(nextValue));
				}
			}
		}
	}

	private ATCommand prepareDRI(final String nextValue) {
		return new ATCommand(AT_SET_HEADER + nextValue);
	}
}