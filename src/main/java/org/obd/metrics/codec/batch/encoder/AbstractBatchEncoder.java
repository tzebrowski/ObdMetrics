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
package org.obd.metrics.codec.batch.encoder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.PidDefinitionCustomization;
import org.obd.metrics.codec.Encoder;
import org.obd.metrics.codec.batch.BatchCodec;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition.Overrides;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractBatchEncoder implements Encoder<BatchObdCommand> {

	protected static final int DEFAULT_BATCH_SIZE = 6;
	protected static final String MODE_22 = "22";
	protected static final String MODE_01 = "01";

	protected final Adjustments adjustments;
	protected final List<ObdCommand> commands;
	protected final Init init;
	protected final BatchCodec codec;

	protected abstract int determineBatchSize(final String key);

	AbstractBatchEncoder(final BatchCodec codec, final Init init, final Adjustments adjustments,
			final List<ObdCommand> commands) {

		this.adjustments = adjustments;
		this.commands = commands;
		this.init = init;
		this.codec = codec;
	}

	@Override
	public List<BatchObdCommand> encode() {
		if (commands.size() == 1) {
			final Map<String, List<ObdCommand>> groupedByModeAndNetwork = groupByModeAndNetwork(commands);
			
			return groupedByModeAndNetwork.entrySet().stream().map(e -> {
				final String key = extractKey(e.getKey());
				
				return ListUtils.partition(e.getValue(), determineBatchSize(key)).stream().map(partitions -> {
					return map(partitions, getPriority(commands.get(0)));
				}).collect(Collectors.toList());
			}).flatMap(List::stream).collect(Collectors.toList());

		} else if (commands.size() <= DEFAULT_BATCH_SIZE) {
			final Map<String, List<ObdCommand>> groupedByMode = groupByModeAndNetwork(commands);
			
			return groupedByMode.entrySet().stream().map(e -> {
				// split by partitions of $BATCH_SIZE size commands
				final String key = extractKey(e.getKey());
				return ListUtils.partition(e.getValue(), determineBatchSize(key)).stream().map(partitions -> {
					return map(partitions, getPriority(partitions.get(0)));
				}).collect(Collectors.toList());

			}).flatMap(List::stream).collect(Collectors.toList());
		} else {
			final Map<String, Map<Integer, List<ObdCommand>>> groupedByModeAndPriority = groupByPriority();
			
			return groupedByModeAndPriority.entrySet().stream().map(entry -> {
				return entry.getValue().entrySet().stream().map(e -> {
					// split by partitions of $BATCH_SIZE size commands
					final String key = extractKey(entry.getKey());
					return ListUtils.partition(e.getValue(), determineBatchSize(key)).stream()
							.map(partition -> {
								return map(partition, e.getKey());
							}).collect(Collectors.toList());
				}).flatMap(List::stream).collect(Collectors.toList());
			}).flatMap(List::stream).collect(Collectors.toList());
		}
	}

	protected String extractKey(String key) {
	    if (key == null) {
	        return "";
	    }
	    final int dotIndex = key.indexOf('.');
	    return dotIndex > 0 ? key.substring(0, dotIndex) : key;
	}

	private Map<String, List<ObdCommand>> groupByModeAndNetwork(List<ObdCommand> commands) {
	    return commands.stream()
	            .collect(Collectors.groupingBy(
	                    this::getGroupKey,
	                    LinkedHashMap::new,
	                    Collectors.toList()
	            ));
	}

	protected Map<String, Map<Integer, List<ObdCommand>>> groupByPriority() {
		return commands.stream()
				.collect(Collectors.groupingBy(f -> getGroupKey(f), Collectors.groupingBy(p -> getPriority(p))));
	}

	protected Integer getPriority(ObdCommand p) {
		if (p == null || p.getPid() == null) {
			return 0;
		}
		final PidDefinitionCustomization override = adjustments.getOverrides().get(p.getPid().getId());
		return (override == null) ? p.getPid().getPriority() : override.getPriority();
	}
	
	protected String getGroupKey(ObdCommand f) {
		if (f == null || f.getPid() == null) {
			return "";
		}
		
		final Overrides overrides = f.getPid().getOverrides();
		final String mode = (overrides != null && overrides.getCanMode().length() > 0) 
				? overrides.getCanMode() 
				: f.getPid().getMode();
				
		return mode + '.' + f.getCanNetwork();
	}
	
	protected BatchObdCommand map(final List<ObdCommand> commands, final int priority) {
		final String query = commands.get(0).getPid().getMode() + " "
				+ commands.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" ")) + " "
				+ (adjustments.getBatchPolicy().isCalculateResponseFrames() ? determineExpectedFramesCount(commands)
						: "");

		return new BatchObdCommand(codec, query, commands, priority);
	}

	protected int determineExpectedFramesCount(final List<ObdCommand> commands) {
		if (commands == null || commands.isEmpty()) {
			return 1;
		}

		int expectedPayloadBytes = 1;

		for (final ObdCommand cmd : commands) {
			final String mode = cmd.getPid().getMode();
			int dataLength = cmd.getPid().getLength();

			int identifierLength = 1;
			if (mode != null && mode.startsWith(MODE_22)) {
				identifierLength = 2;
			}

			expectedPayloadBytes += (identifierLength + dataLength);
		}

		return ((expectedPayloadBytes - 1) / 7) + 1;
	}

	protected int getPIDsLength(final List<ObdCommand> commands) {
		final int length = commands.stream().map(p -> p.getPid().getPid().length() + (2 * p.getPid().getLength()))
				.reduce(0, Integer::sum);

		final String cmd = commands.get(0).getPid().getMode() + " "
				+ commands.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" "));

		log.info("Calculated response length: {} for commands '{}'", length, cmd);

		return length;
	}
}
