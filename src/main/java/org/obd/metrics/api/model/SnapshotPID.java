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
package org.obd.metrics.api.model;

import org.obd.metrics.pid.PidDefinition;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class SnapshotPID {
	private PidDefinition definition;
	private String rawValueHex;
	private Number decodedValue;

	public SnapshotPID(PidDefinition definition, String rawValueHex, Number decodedValue) {
		this.definition = definition;
		this.rawValueHex = rawValueHex;
		this.decodedValue = decodedValue;
	}

	@Override
	public String toString() {
		return String.format("DID: %s | Raw: %-8s | Decoded: %-6s %-5s | %s", definition.getPid(), rawValueHex,
				decodedValue != null ? decodedValue : "N/A",
				definition.getUnits() != null ? definition.getUnits() : "", definition.getDescription());
	}
}