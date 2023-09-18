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
package org.obd.metrics.alert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.pid.PidDefinition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DefaultAlertManager extends ReplyObserver<ObdMetric> implements Alerts {

	private Map<PidDefinition, List<Alert>> allerts = new HashMap<>();

	@Override
	public void onNext(ObdMetric t) {
		if (t.isAlert()) {
			log.error("In alert={}", t);
			append(t);
		}
	}

	@Override
	public void reset() {
		allerts.clear();
	}

	@Override
	public List<Alert> findBy(PidDefinition pid) {
		if (allerts.containsKey(pid)) {
			return allerts.get(pid);
		} else {
			return List.of();
		}
	}
	
	@Override
	public Map<PidDefinition, List<Alert>> findAll(){
		return allerts;
	}
	
	private void append(ObdMetric t) {
		final Alert a = new Alert(t.getValue(), t.getTimestamp());
		if (allerts.containsKey(t.getCommand().getPid())) {
			allerts.get(t.getCommand().getPid()).add(a);
		} else {
			allerts.put(t.getCommand().getPid(), Arrays.asList(a));
		}
	}
}
