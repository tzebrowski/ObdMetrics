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

import org.obd.metrics.pid.PidDefinition;

/**
 * Recognises the PIDs that request the vehicle VIN, by request rather than by description: descriptions
 * differ per profile and may be translated.
 */
final class VinPids {

	// OBD-II mode 09 PID 02 (SAE J1979).
	private static final String OBD_MODE = "09";
	private static final String OBD_PID = "02";

	// UDS ReadDataByIdentifier DID F190 (ISO 14229).
	private static final String UDS_MODE = "22";
	private static final String UDS_DID = "F190";

	private VinPids() {
	}

	static boolean isVin(final PidDefinition pid) {
		final String mode = pid.getMode();
		final String id = pid.getPid();
		if (mode == null || id == null) {
			return false;
		}
		return (OBD_MODE.equals(mode.trim()) && OBD_PID.equals(id.trim()))
				|| (UDS_MODE.equals(mode.trim()) && UDS_DID.equalsIgnoreCase(id.trim()));
	}
}
