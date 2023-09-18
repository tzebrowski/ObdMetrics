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
package org.obd.metrics;

import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class DataCollector extends ReplyObserver<Reply<?>> {

	private boolean info = false;
	
	@Getter
	private final MultiValuedMap<Command, Reply<?>> data = new ArrayListValuedHashMap<Command, Reply<?>>();

	private final MultiValuedMap<PidDefinition, ObdMetric> metrics = new ArrayListValuedHashMap<PidDefinition, ObdMetric>();

	public ObdMetric findSingleMetricBy(PidDefinition pidDefinition) {
		List<ObdMetric> list = (List<ObdMetric>) metrics.get(pidDefinition);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	public List<ObdMetric> findMetricsBy(PidDefinition pidDefinition) {
		return (List<ObdMetric>) metrics.get(pidDefinition);
	}

	public Reply<?> findATResetCommand() {
		final ATCommand key = new ATCommand("Z");
		if (data.containsKey(key)) {
			final List<Reply<?>> collection = (List<Reply<?>>) data.get(key);
			if (!collection.isEmpty()) {
				return collection.get(0);
			}
		} 
		return null;
	}

	@Override
	public void onNext(Reply<?> reply) {
		if (info) {
			log.info("Receive data: {} = {}", reply.getCommand(), reply.toString());
		}else {
			log.trace("Receive data: {}", reply.toString());
		}

		data.put(reply.getCommand(), reply);

		if (reply instanceof ObdMetric) {
			metrics.put(((ObdMetric) reply).getCommand().getPid(), (ObdMetric) reply);
		}
	}
}
