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
package org.obd.metrics.codec.batch;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.codec.batch.mapper.BatchCommandsMapper;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractBatchCodec implements BatchCodec {
	
	protected static final int DEFAULT_BATCH_SIZE = 6;
	protected static final String MODE_22 = "22";
	
	protected final Adjustments adjustments;
	protected final List<ObdCommand> commands;
	protected final String query;
	protected final Init init;
	protected final BatchCodecType codecType;
	protected final BatchCommandsMapper mapper = BatchCommandsMapper.instance();
	
	AbstractBatchCodec(final BatchCodecType codecType, final Init init, final Adjustments adjustments,
			final String query, final List<ObdCommand> commands) {
		this.codecType = codecType;
		this.adjustments = adjustments;
		this.query = query;
		this.commands = commands;
		this.init = init;
	}

	@Override
	public int getCacheHit(final String query) {
		return mapper.getCacheHit(query);
	}

	@Override
	public Map<ObdCommand, ConnectorResponse> decode(final PidDefinition p, final ConnectorResponse connectorResponse) {
		return mapper.convert(query, commands, connectorResponse);
	}

	@Override
	public List<BatchObdCommand> encode() {
		
		if (commands.size() == 1) {
			final Map<String, List<ObdCommand>> groupedByMode = groupByMode();
			return groupedByMode.entrySet().stream().map(e -> {
				return ListUtils.partition(e.getValue(), determineBatchSize(e.getKey())).stream().map(partitions -> {
					return map(partitions, commands.get(0).getPriority());
				}).collect(Collectors.toList());
			}).flatMap(List::stream).collect(Collectors.toList());
		} else if (commands.size() <= DEFAULT_BATCH_SIZE) {
			final Map<String, List<ObdCommand>> groupedByMode = groupByMode();
			return groupedByMode.entrySet().stream().map(e -> {
				// split by partitions of $BATCH_SIZE size commands
				return ListUtils.partition(e.getValue(), determineBatchSize(e.getKey())).stream().map(partitions -> {
					return map(partitions, 0);
				}).collect(Collectors.toList());
			}).flatMap(List::stream).collect(Collectors.toList());
		} else {
			final Map<String, Map<Integer, List<ObdCommand>>> groupedByModeAndPriority = groupByPriority();
			return groupedByModeAndPriority.entrySet().stream().map(entry -> {
				return entry.getValue().entrySet().stream().map(e -> {
					// split by partitions of $BATCH_SIZE size commands
					return ListUtils.partition(e.getValue(), determineBatchSize(entry.getKey())).stream()
							.map(partition -> {
								return map(partition, e.getKey());
							}).collect(Collectors.toList());
				}).flatMap(List::stream).collect(Collectors.toList());
			}).flatMap(List::stream).collect(Collectors.toList());
		}
	}

	private Map<String, List<ObdCommand>> groupByMode() {
		return commands.stream()
				.collect(Collectors.groupingBy(f -> getGroupKey(f)));
	}

	protected Map<String, Map<Integer, List<ObdCommand>>> groupByPriority() {
		return commands.stream().collect(
				Collectors.groupingBy(f -> getGroupKey(f), 
						Collectors.groupingBy(p -> p.getPid().getPriority())));
	}

	protected String getGroupKey(ObdCommand f) {
		return (f.getPid().getOverrides() != null && f.getPid().getOverrides().getCanMode().length() == 0) ? f.getPid().getMode() : f.getPid().getOverrides().getCanMode();
	}

	protected BatchObdCommand map(final List<ObdCommand> commands, final int priority) {
		final String query = commands.get(0).getPid().getMode() + " "
				+ commands.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" ")) + " "
				+ (adjustments.getBatchPolicy().isResponseLengthEnabled() ? determineNumberOfLines(commands) : "");

		final BatchCodec codec = BatchCodec.builder()
				.codecType(codecType)
				.init(init)
				.adjustments(adjustments)
				.query(query)
				.commands(commands)
				.build();
		
		return new BatchObdCommand(codec, query, commands, priority);
	}

	protected int determineBatchSize(final String mode) {
		return DEFAULT_BATCH_SIZE;
	}

	protected int determineNumberOfLines(final List<ObdCommand> commands) {
		// 3 00B0:62194F2E65101:0348193548
		// 6 26 00E0:410BFF0C00001:11000D000400062:80AAAAAAAAAAAA
		// 5 22 00C0:410C000011001:0D0004000680AA
		// 4 1 0090:4111000D00041:000680AAAAAAAA
		// 3 1 410D0004000680
		// 2 8 4104000680
		// 10
		// 14
		final int length = getPIDsLength(commands);

		if (length < 12) {
			return 1;
		} else if (length >= 12 && length <= 24) {
			return 2;
		} else if (length >= 25 && length <= 37) {
			return 3;
		} else if (length >= 38 && length <= 49) {
			return 4;
		} else if (length > 49 && length <= 62) {
			return 5;
		} else {
			return 6;
		}
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
