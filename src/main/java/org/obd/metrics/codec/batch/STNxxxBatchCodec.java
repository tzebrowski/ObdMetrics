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
package org.obd.metrics.codec.batch;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.command.obd.BatchObdCommand;
import org.obd.metrics.command.obd.ObdCommand;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class STNxxxBatchCodec extends AdjustableBatchSizeCodec {

	private static final int PRIORITY_0 = 0;
	private static final int MODE_22_BATCH_SIZE = 11;

	STNxxxBatchCodec(final Init init, final Adjustments adjustments, final String query,
			final List<ObdCommand> commands) {
		super(BatchCodecType.STNxxx, init, adjustments, query, commands, MODE_22_BATCH_SIZE, DEFAULT_BATCH_SIZE);
	}

	@Override
	protected BatchObdCommand map(List<ObdCommand> commands, int priority) {

		final StringBuffer query = new StringBuffer();
		query.append("STPX ");

		init.getHeaders().stream().filter(p -> p.getMode().equals(getGroupKey(commands.get(0)))).findFirst()
				.ifPresent(h -> {
					query.append("H:");
					query.append(h.getHeader());
					query.append(", ");
				});

		final String data = commands.get(0).getMode() + " "
				+ commands.stream().map(e -> e.getPid().getPid()).collect(Collectors.joining(" "));

		query.append("D:");
		query.append(data);

		if (adjustments.getBatchPolicy().isResponseLengthEnabled()) {
			query.append(", R:");
			query.append(determineNumberOfLines(commands));
		}

		log.info("STNxxx: Build query for STNxxx chip = {}, priority: {}", query, priority);
		final BatchCodec codec = BatchCodec.instance(codecType, init, adjustments, query.toString(), commands);
		return new BatchObdCommand(codec, query.toString(), commands, priority);
	}

	@Override
	protected Map<String, Map<Integer, List<ObdCommand>>> groupByPriority() {
		if (adjustments.getStNxx().isPromoteAllGroupsEnabled()) {
			
			// promoted to priority 0
			final Set<Long> priority0 = findPromotedPIDs(MODE_22);
			log.info("STNxxx: Considered P0 PIDs: {}", priority0);

			// append priority 0
			commands.stream().filter(p -> p.getPriority() == PRIORITY_0).map(p -> p.getPid().getId()).forEach(p -> {
				priority0.add(p);
			});

			log.info("STNxxx: All P0 PIDs {}", priority0);
			final Set<Long> all = commands.stream().map(p -> p.getPid().getId()).collect(Collectors.toSet());
			final Set<Long> diff = new HashSet<Long>();
			diff.addAll(CollectionUtils.subtract(all, priority0));

			final int diffPrio = 
					commands.stream().
					filter(p->diff.contains(p.getPid().getId()) && p.getMode().equals(MODE_22))
					.min(Comparator.comparing(ObdCommand::getPriority)).get().getPriority();
			log.info("STNxxx: All P{} PIDs: {}",diffPrio, diff);

					
			final Map<Long, Integer> maps = new HashMap<>();
			all.forEach(p -> maps.put(p, PRIORITY_0));
			diff.forEach(p -> maps.put(p, diffPrio));

			return aggregate(maps);

		} else if (adjustments.getStNxx().isPromoteSlowGroupsEnabled()) {
			final Set<Long> promotedToPriority0 = findPromotedPIDs(MODE_22);
			log.info("STNxxx: PIDs considered for aggregation: {}", promotedToPriority0);
			final Map<Long,Integer> aa = new HashMap<>();
			promotedToPriority0.forEach( p-> aa.put(p,PRIORITY_0));

			return aggregate(aa);
		} else {
			return commands.stream().collect(
					Collectors.groupingBy(f -> getGroupKey(f), Collectors.groupingBy(p -> p.getPid().getPriority())));
		}
	}

	private Map<String, Map<Integer, List<ObdCommand>>> aggregate(Map<Long,Integer> ids) {
		return commands.stream().collect(Collectors.groupingBy(f -> {
			return getGroupKey(f);
		}, Collectors.groupingBy(p -> {
			if (ids.containsKey(p.getPid().getId())) {
				return ids.get(p.getPid().getId());
			} else {
				return p.getPid().getPriority();
			}
		})));
	}

	private Set<Long> findPromotedPIDs(String mode) {
		final Set<Long> promotedPIDs = new HashSet<>();
		final int numberOfP0 = (int) commands.stream().filter(p -> p.getMode().equals(mode))
				.filter(p -> p.getPid().getPriority() == PRIORITY_0).count();

		final int batchSize = determineBatchSize(mode);
		log.info("STNxxx: Determined batchSize={}", batchSize);
		final int diffToFill = determineBatchSize(mode) - numberOfP0;
		log.info("STNxxx: P0 size: {}, we can pickup: {} more PIDs with lower priorities", numberOfP0, diffToFill);
		for (int i = 0, cnt = 0; i < commands.size(); i++) {
			final ObdCommand item = commands.get(i);
			if (item.getPriority() == 1 || item.getPriority() == 2) {
				promotedPIDs.add(item.getPid().getId());
				cnt++;
				if (cnt == diffToFill) {
					break;
				}
			}
		}
		return promotedPIDs;
	}
}
