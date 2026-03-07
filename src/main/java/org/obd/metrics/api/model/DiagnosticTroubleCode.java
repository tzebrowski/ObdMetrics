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

import java.util.List;

import org.obd.metrics.command.dtc.DtcComponent;

import lombok.EqualsAndHashCode;
import lombok.Getter;


@EqualsAndHashCode(of = "standardCode")
@Getter
public final class DiagnosticTroubleCode {
	private final String standardCode;
	private final String failureTypeByte;
	private final String rawHex;
	private final int statusMask;
	private final List<String> activeStatuses;

	// New Component Breakdown
	private final DtcComponent system;
	private final DtcComponent category;
	private final DtcComponent subsystem;
	private final DtcComponent failureType;
	private final String description;
	
	public DiagnosticTroubleCode(String standardCode, 
			String failureTypeByte, 
			String rawHex, 
			String description,
			int statusMask,
			List<String> activeStatuses, 
			DtcComponent system, 
			DtcComponent category, 
			DtcComponent subsystem,
			DtcComponent failureType) {
		
		this.standardCode = standardCode;
		this.failureTypeByte = failureTypeByte;
		this.rawHex = rawHex;
		this.description = description;
		this.statusMask = statusMask;
		this.activeStatuses = activeStatuses;
		this.system = system;
		this.category = category;
		this.subsystem = subsystem;
		this.failureType = failureType;
	}

	@Override
	public String toString() {
		return String.format(
                "  {\n    \"DTC\": \"%s-%s\",\n    \"Description\": \"%s\",\n    \"Raw Hex\": \"%s\",\n    \"System\": %s,\n    \"Category\": %s,\n    \"Subsystem\": %s,\n    \"Failure Type\": %s,\n    \"Status Mask\": \"0x%02X\",\n    \"Statuses\": %s\n  }",
                standardCode, failureType.getCode(), description, rawHex, system, category, subsystem, failureType, statusMask, activeStatuses
            );
	}
}