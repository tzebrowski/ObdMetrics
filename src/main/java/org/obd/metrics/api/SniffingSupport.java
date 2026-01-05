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
package org.obd.metrics.api;

import org.obd.metrics.api.model.SniffingPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.CommandType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.ValueType;

interface SniffingSupport {

	static void updateCommandBuffer(SniffingPolicy sniffingPolicy, CommandsBuffer commandsBuffer) {

		if (sniffingPolicy.isEnabled() && sniffingPolicy.getStNxx().isEnabled()
				&& !sniffingPolicy.getStNxx().getFilters().isEmpty()) {
			commandsBuffer.addLast(new ObdCommand("STFAC"));

			sniffingPolicy.getStNxx().getFilters().forEach(filter -> {
				commandsBuffer.addLast(new ObdCommand("STFPA " + filter));
			});
		}
	}

	static PidDefinition pid(SniffingPolicy sniffingPolicy) {

		if (sniffingPolicy.getStNxx().isEnabled()) {
			if (sniffingPolicy.getStNxx().getFilters().isEmpty()) {
				return new PidDefinition(Workflow.SNIFFING_PID_ID, "STMA", "Sniffing PIDs  with STMA", 0, 0,
						ValueType.INT, CommandType.AT);

			} else {
				return new PidDefinition(Workflow.SNIFFING_PID_ID, "STM", "Sniffing PIDs with ST M", 0, 0,
						ValueType.INT, CommandType.AT);

			}
		} else {
			return new PidDefinition(Workflow.SNIFFING_PID_ID, "ATMA", "Sniffing PIDs with AT MA", 0, 0, ValueType.INT,
					CommandType.AT);
		}
	}
}
