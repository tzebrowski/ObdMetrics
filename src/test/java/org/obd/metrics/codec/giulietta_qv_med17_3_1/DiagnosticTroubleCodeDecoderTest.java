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
package org.obd.metrics.codec.giulietta_qv_med17_3_1;

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

	// Decoded DTCs are tagged with PidDefinition's default module ("ecu") when no explicit
	// module/header is set, and module is part of DiagnosticTroubleCode's equals()/hashCode().
	private static DiagnosticTroubleCode dtc(String standardCode, String failureTypeByte, String description) {
		final DiagnosticTroubleCode dtc = new DiagnosticTroubleCode(standardCode, failureTypeByte, null, description,
				0, null, null, null, null, null);
		dtc.setModule("ecu");
		return dtc;
	}

	@Test
	public void errors_available_case_0() {
		final String rx = "7F197804B0:5902CF0191131:8F068511CDD6012:870FD706870FD73:00920FD702870F4:0121148F0221145:8F0190170F01206:148F0220148F067:21150F01001C0F8:0230158F0105159:0F0235158F0115A:158F0500640F";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("alfa.json");
		final PidDefinition pid = registry.findBy(26000l);


		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeReadCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
		
		Assertions.assertThat(list).isNotEmpty();
		Assertions.assertThat(list.size()).isEqualTo(18);
		
		Assertions.assertThat(list)
			.contains(dtc("P0191", "13", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0685", "11", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("U1601", "87", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("U1706", "87", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("U1700", "92", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("U1702", "87", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0121", "14", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0190", "17", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0120", "14", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0220", "14", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0621", "15", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0100", "1C", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0230", "15", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0105", "15", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0235", "15", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0115", "15", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0500", "65", DtcDictionary.UNKNOWN_DTC));
		
	}

	
	@Test
	public void erros_available_case_1() {
		final String rx = "00F0:5902CF26E4001:482BC10048D0082:00480";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("alfa.json");
		final PidDefinition pid = registry.findBy(26000l);


		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeReadCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
		
		Assertions.assertThat(list)
			.contains(dtc("P26E4", "00", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P2BC1", "00", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("U1008", "00", DtcDictionary.UNKNOWN_DTC));
		
	}

	@Test
	public void errors_available_case_2() {
		final String rx = "5902CFC4058108";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("alfa.json");
		final PidDefinition pid = registry.findBy(26000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeReadCodec().decode(pid, ConnectorResponseFactory.wrap(rx.getBytes()));
		Assertions.assertThat(list).contains(dtc("U0405", "81", DtcDictionary.UNKNOWN_DTC));

	}

	@Test
	public void errors_available_case_3() {
		// C405810
		final String rx = "7F197800B0:5902CF0191111:08C4058108";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("alfa.json");
		final PidDefinition pid = registry.findBy(26000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeReadCodec().decode(pid, ConnectorResponseFactory.wrap(rx.getBytes()));
		Assertions.assertThat(list)
			.contains(dtc("P0191", "11", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("U0405", "81", DtcDictionary.UNKNOWN_DTC));

	}

	
	@Test
	public void erros_available_case_4() {
		final String rx = "7F19783BB0:5902CF0611471:400327124003272:114003281240033:281140033212404:033211400333125:400333114001916:1240068511CD067:064640060642408:068872400657739:40001012400010A:11400010134000B:11624005041340C:12262940132549D:40063829400638E:98400638864006F:381340168022400:168262401684161:401684264016872:174016871640163:886240168667404:16891C400133265:400606444006016:454000131240007:1311400";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("alfa.json");
		final PidDefinition pid = registry.findBy(26000l);


		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeReadCodec().decode(pid,
				ConnectorResponseFactory.wrap(rx.getBytes()));
		
		Assertions.assertThat(list).isNotEmpty();
		Assertions.assertThat(list.size()).isEqualTo(40);
		
		
		Assertions.assertThat(list)
			.contains(dtc("P0611", "47", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0327", "12", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0327", "11", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0328", "11", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0328", "12", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0332", "11", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0332", "12", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0332", "11", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0332", "12", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0191", "12", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0685", "11", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0606", "46", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0606", "42", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0688", "72", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0657", "73", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0010", "12", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0010", "13", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0011", "62", DtcDictionary.UNKNOWN_DTC))
			.contains(dtc("P0504", "13", DtcDictionary.UNKNOWN_DTC));
			
	}

	
	@Test
	public void no_errors_available() {
		// C405810
		final String rx = "5902CF";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("alfa.json");
		final PidDefinition pid = registry.findBy(26000l);

		final List<DiagnosticTroubleCode> list = new DiagnosticTroubleCodeReadCodec().decode(pid, ConnectorResponseFactory.wrap(rx.getBytes()));
		Assertions.assertThat(list).isEmpty();
	}
}
