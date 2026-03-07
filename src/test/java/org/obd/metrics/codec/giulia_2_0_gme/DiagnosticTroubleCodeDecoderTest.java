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
package org.obd.metrics.codec.giulia_2_0_gme;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeCodec;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class DiagnosticTroubleCodeDecoderTest {

	
	@Test
	public void erros_available_case_1() {
		// P26E4-00
		// P2BC1-00
		// U1008-00
		final String rx = "00F0:5902CF26E4001:482BC10048D0082:00480";
		final PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
		Assertions.assertThat(list).contains(new DiagnosticTroubleCode("P26E4", "00", null, null, 0, null, null, null, null, null))
				.contains(new DiagnosticTroubleCode("P2BC1", "00", null, null, 0, null, null, null, null, null))
				.contains(new DiagnosticTroubleCode("U1008", "00", null, null, 0, null, null, null, null, null));
	}

	@Test
	public void error_available_case_2() {
		final String rx = "5902CFC4058108";
		final PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
		Assertions.assertThat(list).contains(new DiagnosticTroubleCode("U0405", "81", null, null, 0, null, null, null, null, null));
	}
	

	@Test
	public void error_available_case_3() {
		// C405810
		final String rx = "7F197800B0:5902CF0191111:08C4058108";
		final PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
		Assertions.assertThat(list)
			.contains(new DiagnosticTroubleCode("P0191", "11", null, null, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("U0405", "81", null, null, 0, null, null, null, null, null));
	}

	@Test
	public void no_errors_available_case_1() {
		// C405810
		final String rx = "5902CF";
		final PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
		Assertions.assertThat(list).isEmpty();
	}

	@Test
	public void available_errors_case_4() {
		// C405810
		final String rx = "7F19785902CF00101348";
		final PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
		Assertions.assertThat(list).contains(new DiagnosticTroubleCode("P0010", "13", null, null, 0, null, null, null, null, null));
	}
}
