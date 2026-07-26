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
package org.obd.metrics.command.discovery;

import org.obd.metrics.command.Command;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * UDS TesterPresent (3E 00) probe used to check whether a module answers at
 * a given CAN header, for ECU auto-discovery. Built directly from the
 * header, not from a {@link org.obd.metrics.pid.PidDefinition} - discovery
 * targets are ad-hoc candidate headers, not registry-managed PIDs.
 */
@EqualsAndHashCode(of = { "header" }, callSuper = false)
public class ModuleDiscoveryCommand extends Command {

	@Getter
	private final String header;

	public ModuleDiscoveryCommand(final String header) {
		super("3E00", "3E", "Module discovery probe: " + header);
		this.header = header;
	}
}
