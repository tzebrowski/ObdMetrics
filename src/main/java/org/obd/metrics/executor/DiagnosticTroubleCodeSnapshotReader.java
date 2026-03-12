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
package org.obd.metrics.executor;

import java.util.HashSet;
import java.util.Set;

import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.command.dtc.UdsSnapshotResponse;

import lombok.Getter;

final class DiagnosticTroubleCodeSnapshotReader extends ReplyObserver<ObdMetric> {

	@Getter
	private volatile Set<UdsSnapshotResponse> value = new HashSet<UdsSnapshotResponse>();

	@Override
	public void onNext(ObdMetric reply) {

		if (reply.getCommand().getPid().getId() == 999999) {
			value.add((UdsSnapshotResponse)reply.getValue());
		}
	}
	
	void reset() {
		value.clear();
	}
}
