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
 * Recognises the PIDs that request the vehicle VIN by their category ({@code "category": "vin"} in the PID resource
 * file), never by description: descriptions differ per profile and are translated.
 */
public final class VinPids {

	public static final String CATEGORY = "vin";

	private VinPids() {
	}

	public static boolean isVin(final PidDefinition pid) {
		return CATEGORY.equalsIgnoreCase(pid.getCategory());
	}

	/**
	 * The VIN to keep when a VIN PID answers: the reply trimmed, unless a real VIN is already known. A profile may
	 * define both 09 02 and 22 F190, and an ECU without a programmed VIN answers with an empty one.
	 */
	public static String preferred(final String current, final String reply) {
		if (reply == null || (current != null && !current.isEmpty())) {
			return current;
		}
		return reply.trim();
	}
}
