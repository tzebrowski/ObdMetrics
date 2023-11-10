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
package org.obd.metrics.command.obd;

import org.obd.metrics.command.Command;
import org.obd.metrics.pid.PidDefinition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = { "pid" }, callSuper = false)
public class ObdCommand extends Command {

	@Getter
	protected PidDefinition pid;

	public ObdCommand(final String query) {
		super(query, null, "Query: " + query);
	}

	public ObdCommand(final PidDefinition pid) {
		super(pid.getQuery(), pid.getService(), pid.getDescription(), pid.getOverrides().getCanMode());
		this.pid = pid;
	}

	public int getPriority() {
		return pid.getPriority();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[pid=");
		if (pid != null) {
			builder.append(pid.getDescription());
		}

		builder.append(", query=");
		builder.append(query);
		builder.append("]");
		return builder.toString();
	}
}
