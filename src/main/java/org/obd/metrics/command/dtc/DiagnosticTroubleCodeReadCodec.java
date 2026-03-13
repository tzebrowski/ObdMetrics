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

import java.util.Collections;
import java.util.List;

import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DiagnosticTroubleCodeReadCodec implements Codec<Void, List<DiagnosticTroubleCode>> {
	private UdsMultiFrameDtcParser parser = new UdsMultiFrameDtcParser();
	
	@Override
	public List<DiagnosticTroubleCode> decode(final PidDefinition pid, final ConnectorResponse connectorResponse) {

		if (connectorResponse.isEmpty()) {
			return Collections.emptyList();
		} else {

			final UdsResponse udsResponse = parser.parse(connectorResponse.getMessage());

			if (udsResponse.hasError()) {
				log.error("Error: {}", udsResponse.getErrorMessage());
			} else {

				if (log.isDebugEnabled()) {
					log.debug("Reassembled Payload: {}", udsResponse.getRawPayload());
					log.debug("\nECU Supported Statuses (Mask: 0x {})", udsResponse.getStatusAvailabilityMaskHex());
					for (final String status : udsResponse.getSupportedStatuses()) {
						log.debug(" - {}", status);
					}
	
					log.debug("\n--- Extracted DTCs ---");
					for (final DiagnosticTroubleCode dtc : udsResponse.getDtcs()) {
						log.debug("{}", dtc);
					}	
				}	
				return udsResponse.getDtcs();
			}
		}
		return Collections.emptyList();
	}
}
