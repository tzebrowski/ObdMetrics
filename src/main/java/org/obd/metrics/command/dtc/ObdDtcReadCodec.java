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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.translation.TranslationProvider;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Decodes OBD-II services 03 (stored), 07 (pending) and 0A (permanent), the {@code dtcObdRead} PIDs: the answer
 * byte (the PID's success code, 0x40 + service), on CAN a
 * count byte, then two bytes per code. Every emissions ECU answers a functional request, so several answers can
 * follow one another in the message; they are read in turn. A multi-frame answer after another ECU's answer is not
 * separated and ends the parse (one ECU per multi-frame reply).
 */
@Slf4j
public final class ObdDtcReadCodec implements Codec<Void, List<DiagnosticTroubleCode>> {

	private static final int CONFIRMED_DTC = 0x08;
	private static final int PENDING_DTC = 0x04;

	private final UdsMultiFrameDtcParser parser;

	public ObdDtcReadCodec() {
		this.parser = new UdsMultiFrameDtcParser();
	}

	public ObdDtcReadCodec(TranslationProvider translationProvider) {
		this.parser = new UdsMultiFrameDtcParser(translationProvider);
	}

	@Override
	public List<DiagnosticTroubleCode> decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {
		if (connectorResponse.isEmpty()) {
			return Collections.emptyList();
		}

		final String service = pid.getMode().toUpperCase();
		final String payload = parser.extractPayload(connectorResponse.getMessage().toUpperCase());

		if (payload.startsWith("7F" + service)) {
			log.info("Service {} refused: {}", service, payload);
			return new DtcReadReply(DtcModuleStatus.REFUSED, Collections.emptyList());
		}

		final String answer = pid.getSuccessCode().toUpperCase();
		final boolean permanent = "0A".equals(service);
		final int statusMask = "03".equals(service) ? CONFIRMED_DTC : "07".equals(service) ? PENDING_DTC : 0;

		final List<DiagnosticTroubleCode> codes = new ArrayList<>();
		int pos = 0;
		while (payload.startsWith(answer, pos) && pos + 4 <= payload.length()) {
			final int count = Integer.parseInt(payload.substring(pos + 2, pos + 4), 16);
			pos += 4;
			for (int i = 0; i < count && pos + 4 <= payload.length(); i++, pos += 4) {
				final String hex = payload.substring(pos, pos + 4);
				if (!"0000".equals(hex)) {
					final DiagnosticTroubleCode dtc = parser.decodeObdDtc(hex, statusMask, permanent);
					dtc.setModule(pid.getModule());
					codes.add(dtc);
				}
			}
		}

		if (codes.isEmpty() && !payload.startsWith(answer)) {
			log.warn("Not a service {} answer: {}", service, payload);
		}
		return codes;
	}
}
