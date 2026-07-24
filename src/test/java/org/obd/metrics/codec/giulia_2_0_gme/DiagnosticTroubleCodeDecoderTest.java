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
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeReadCodec;
import org.obd.metrics.command.dtc.DtcDictionary;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class DiagnosticTroubleCodeDecoderTest {

	@Test
	public void erros_available_case_0() {
		final String rx = "7F19780370:5902CF0191131:8FD601870E01212:148F0221148F013:90170F0120148F4:0220148F0621155:0F01001C0F02306:158F0105150F027:35158F0115158F";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final PidDefinition pid = registry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeReadCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
	
		Assertions.assertThat(list)
			.contains(new DiagnosticTroubleCode("P0191", "13", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("U1601", "87", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0121", "14", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0221", "14", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0190", "17", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0120", "14", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0220", "14", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0621", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0100", "1C", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0230", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0105", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0235", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0115", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null));
	}

	
	@Test
	public void erros_available_case_1() {
		// P26E4-00
		// P2BC1-00
		// U1008-00
		final String rx = "00F0:5902CF26E4001:482BC10048D0082:00480";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final PidDefinition pid = registry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeReadCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
	
		Assertions.assertThat(list)
			.contains(new DiagnosticTroubleCode("P26E4", "00", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P2BC1", "00", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("U1008", "00", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null));
	}

	@Test
	public void error_available_case_2() {
		final String rx = "5902CFC4058108";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final PidDefinition pid = registry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeReadCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
		Assertions.assertThat(list).contains(new DiagnosticTroubleCode("U0405", "81", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null));
	}
	

	@Test
	public void error_available_case_3() {
		// C405810
		final String rx = "7F197800B0:5902CF0191111:08C4058108";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final PidDefinition pid = registry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeReadCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
		Assertions.assertThat(list)
			.contains(new DiagnosticTroubleCode("P0191", "11", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("U0405", "81", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null));
	}
	
	
	@Test
	public void available_errors_case_4() {
		// C405810
		final String rx = "7F19785902CF00101348";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final PidDefinition pid = registry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeReadCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
		Assertions.assertThat(list).contains(new DiagnosticTroubleCode("P0010", "13", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null));
	}
	

	@Test
	public void no_errors() {
		// C405810
		final String rx = "5902CF";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final PidDefinition pid = registry.findBy(27000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeReadCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
		Assertions.assertThat(list).isEmpty();
	}

	@Test
	public void decodedDtcsAreTaggedWithTheirModule() {
		final String rx = "00F0:5902CF26E4001:482BC10048D0082:00480";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final PidDefinition pid = registry.findBy(27000l);

		final List<DiagnosticTroubleCode> engineList = new DiagnosticTroubleCodeReadCodec()
				.decode(pid.withModule("Engine"), ConnectorResponseFactory.wrap(rx.getBytes()));
		final List<DiagnosticTroubleCode> absList = new DiagnosticTroubleCodeReadCodec()
				.decode(pid.withModule("ABS"), ConnectorResponseFactory.wrap(rx.getBytes()));

		Assertions.assertThat(engineList).isNotEmpty();
		Assertions.assertThat(engineList).extracting(DiagnosticTroubleCode::getModule).containsOnly("Engine");

		Assertions.assertThat(absList).isNotEmpty();
		Assertions.assertThat(absList).extracting(DiagnosticTroubleCode::getModule).containsOnly("ABS");

		// The underlying registry PID must stay untouched by withModule() - it's a clone.
		Assertions.assertThat(pid.getModule()).isEqualTo("ecu");
	}
}
