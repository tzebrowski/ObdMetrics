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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.obd.metrics.pid.PIDsGroup;
import org.obd.metrics.pid.PidDefinition;

/**
 * The generic OBD-II trouble code services, sent instead of UDS 19 02 / 14 to a module whose header is the OBD-II
 * functional address (the table's "legacy" entry): such a request reaches every emissions ECU, which answer OBD-II
 * services, not UDS ones.
 */
public final class ObdDtcRequests {

	private static final List<String> FUNCTIONAL_HEADERS = Arrays.asList("7DF", "DB33F1", "18DB33F1");
	private static final long BASE_ID = 999990;

	private static final List<PidDefinition> READS = Collections.unmodifiableList(Arrays.asList(
			read(BASE_ID, "03", "DTC Read (stored)"),
			read(BASE_ID + 1, "07", "DTC Read (pending)"),
			read(BASE_ID + 2, "0A", "DTC Read (permanent)")));

	private static final PidDefinition CLEAR = clear();

	private ObdDtcRequests() {
	}

	/** True for the OBD-II functional request header, 11-bit 7DF or 29-bit (18)DB33F1, in any spelling. */
	public static boolean isObdFunctionalHeader(final String header) {
		return header != null && FUNCTIONAL_HEADERS.contains(header.replaceAll("\\s+", "").toUpperCase());
	}

	/** Services 03, 07 and 0A, in that order. */
	public static List<PidDefinition> reads() {
		return READS;
	}

	/** Service 04: clears stored and pending codes, freeze frames and readiness of every emissions ECU. */
	public static PidDefinition clearRequest() {
		return CLEAR;
	}

	private static PidDefinition read(final long id, final String service, final String description) {
		final PidDefinition pid = new PidDefinition(id, "", service, description, service,
				ObdDtcReadCodec.class.getName());
		pid.setGroup(PIDsGroup.DTC_READ);
		pid.setSuccessCode(answer(service));
		return pid;
	}

	private static PidDefinition clear() {
		final PidDefinition pid = new PidDefinition(BASE_ID + 3, "", "04", "DTC Clear (OBD-II)", "04",
				DiagnosticTroubleCodeClearCodec.class.getName());
		pid.setGroup(PIDsGroup.DTC_CLEAR);
		pid.setSuccessCode(answer("04"));
		return pid;
	}

	// 0x40 + service. Set explicitly: PidDefinition derives it from a decimal mode, which "0A" is not.
	static String answer(final String service) {
		return String.format("%02X", 0x40 + Integer.parseInt(service, 16));
	}
}
