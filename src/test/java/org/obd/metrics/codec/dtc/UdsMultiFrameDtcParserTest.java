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
package org.obd.metrics.codec.dtc;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.command.dtc.UdsMultiFrameDtcParser;
import org.obd.metrics.command.dtc.UdsResponse;

public class UdsMultiFrameDtcParserTest {

	@Test
	public void multiFrameResponseTest(){
		
		final String multiFrameData = "7F19780370:5902CF0191131:8FD601870E01212:148F0221148F013:90170F0120148F4:0220148F0621155:0F01001C0F02306:158F0105150F027:35158F0115158F";
		final UdsMultiFrameDtcParser parser = new UdsMultiFrameDtcParser();

		final UdsResponse result = parser.parse(multiFrameData);
		
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.hasError()).isFalse();
		Assertions.assertThat(result.getRawPayload()).isNotNull();
		Assertions.assertThat(result.getDtcs()).isNotNull().isNotEmpty().hasSize(13);
		Assertions.assertThat(result.getStatusAvailabilityMaskHex()).isNotNull().isNotEmpty().hasSize(2);
		

		System.out.println("Reassembled Payload: " + result.getRawPayload());
		System.out.println("\nECU Supported Statuses (Mask: 0x" + result.getStatusAvailabilityMaskHex() + "):");
		for (String status : result.getSupportedStatuses()) {
			System.out.println(" - " + status);
		}

		System.out.println("\n--- Extracted DTCs ---");
		for (DiagnosticTroubleCode dtc : result.getDtcs()) {
			System.out.println(dtc);
		}
	}

}
