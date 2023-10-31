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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.codec.batch.BatchCodec;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.CommandType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
final class CommandsSuplier implements Supplier<List<ObdCommand>> {

	private List<ObdCommand> commands;

	private final PidDefinitionRegistry pidRegistry;
	private final Adjustments adjustements;
	private final Query query;
	private final Init init;

	@Override
	public List<ObdCommand> get() {
		if (commands == null) {
			commands = build(query);
		}
		return commands;
	}

	private List<ObdCommand> build(Query query) {
		final List<ObdCommand> commands = query
				.getPids()
				.stream()
				.map(idToCommand())
				.filter(Objects::nonNull)
				.sorted((c1, c2) -> c2.getPid().compareTo(c1.getPid()))
				.collect(Collectors.toList());
		
		final List<ObdCommand> result = new ArrayList<>();
		if (adjustements.getBatchPolicy().isEnabled()) {
			// collect first commands that support batch fetching
			final List<ObdCommand> obdCommands = commands.stream()
					.filter(p -> CommandType.OBD.equals(p.getPid().getCommandType()))
					.filter(p-> isBatchEnabledForPid(p))
					.filter(distinctByKey(c -> c.getPid().getPid()))
					.collect(Collectors.toList());

			final List<BatchObdCommand> batchEncoded = BatchCodec.builder()
					.init(init)
					.adjustments(adjustements)
					.commands(obdCommands)
					.build()
					.encode();
			
			result.addAll(batchEncoded);
			// add at the end commands that does not support batch fetching
			result.addAll(commands.stream().filter(p -> !CommandType.OBD.equals(p.getPid().getCommandType()))
					.collect(Collectors.toList()));
			
			commands.stream()
					.filter(p -> CommandType.OBD.equals(p.getPid().getCommandType()))
					.filter(p-> !isBatchEnabledForPid(p))
					.filter(distinctByKey(c -> c.getPid().getPid()))
					.forEach(c-> { 
						result.addAll(BatchCodec.builder()
								.init(init)
								.adjustments(adjustements)
								.commands(Arrays.asList(c))
								.build()
								.encode());
					});
			
		} else {
   		    result.addAll(commands.stream().map(command -> { 
				if (command.getPid().isMultiSegmentAnswer()) {
					return BatchCodec.builder()
							.init(init)
							.adjustments(adjustements)
							.commands(Arrays.asList(command))
							.build()
							.encode().get(0);
				} else { 
					return command;
				} 
			}).collect(Collectors.toSet()));
		}
		
		log.info("Build target commands list: {}", result);
		return result;
	}

	private boolean isBatchEnabledForPid(ObdCommand p) {
		return p.getPid().getOverrides().isBatchEnabled();
	}

	private <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		final Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	private Function<? super Long, ? extends ObdCommand> idToCommand() {
		return pid -> {
			final PidDefinition findBy = pidRegistry.findBy(pid);
			return findBy == null ? null : new ObdCommand(findBy);
		};
	}
}
