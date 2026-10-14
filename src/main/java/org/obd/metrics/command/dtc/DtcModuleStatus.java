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
package org.obd.metrics.command.dtc;

/**
 * How a module answered a scheduled DTC read: {@link #RESPONDED} (codes or none), {@link #REFUSED} (negative
 * response to service 19) or {@link #NO_RESPONSE} (no reply at all: NO DATA, timeout or another adapter error).
 */
public enum DtcModuleStatus {
	RESPONDED, REFUSED, NO_RESPONSE;

	/**
	 * The status of a module's raw DTC read reply. NRC 78 (response pending) frames ahead of the answer are skipped,
	 * as the DTC parser does.
	 */
	public static DtcModuleStatus of(final String rawReply) {
		if (rawReply == null) {
			return NO_RESPONSE;
		}
		final String cleaned = rawReply.replaceAll("\\s+", "").toUpperCase().replaceFirst("^(?:7F[0-9A-F]{2}78)+", "");
		if (cleaned.isEmpty()) {
			return NO_RESPONSE;
		}
		// Single frame "7F19xx", or the payload of the first ISO-TP frame ("0:7F19...").
		return cleaned.startsWith("7F19") || cleaned.matches("^(?:[0-9A-F]{3})?0:7F19.*") ? REFUSED : RESPONDED;
	}
}
