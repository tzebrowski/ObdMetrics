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
package org.obd.metrics.command.routine;

import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = { "pid" }, callSuper = false)
public class RoutineCommand extends Command {

	@Getter
	private final PidDefinition pid;

	public RoutineCommand(final PidDefinition pid) {
		super(pid.getQuery(), pid.getMode(), "Routine: " + pid.getQuery());
		this.pid = pid;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[query=");
		builder.append(query);
		builder.append("]");
		return builder.toString();
	}
}
