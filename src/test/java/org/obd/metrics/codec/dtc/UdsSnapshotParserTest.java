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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeSnapshotCodec;
import org.obd.metrics.pid.PIDsGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponseFactory;
import org.obd.metrics.command.dtc.UdsSnapshotParser;
import org.obd.metrics.command.dtc.UdsSnapshotResponse;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;

public class UdsSnapshotParserTest {

	@Test
	public void multiFrameResponseTest() {
							  	
		final String rawData =  "0310:59040115158F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83";
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("alfa.json");
		final UdsSnapshotParser parser = new UdsSnapshotParser(registry, "JavaScript");
		final UdsSnapshotResponse result = parser.parse(rawData);
		
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.hasError()).isFalse();
		Assertions.assertThat(result.getErrorMessage()).isEmpty();
		
		final Map<String, Number> values = new HashMap<String, Number>();
		
		result.getExtractedDids().forEach( p-> {
			values.put(p.getDefinition().getPid(),p.getDecodedValue());
		});
		
		Assertions.assertThat(values).containsEntry("1008", 367);
		Assertions.assertThat(values).containsEntry("1009", 0);
		Assertions.assertThat(values).containsEntry("200A", 847);
		Assertions.assertThat(values).containsEntry("6082", 21.0);
		Assertions.assertThat(values).containsEntry("1000", 0);
		Assertions.assertThat(values).containsEntry("181D", 100.01);
		Assertions.assertThat(values).containsEntry("1003", -39);
		Assertions.assertThat(values).containsEntry("181D", 100.01);
		Assertions.assertThat(values).containsEntry("1935", -39);
		Assertions.assertThat(values).containsEntry("1862", 1585.45);
		Assertions.assertThat(values).containsEntry("1812", 0.0);
		Assertions.assertThat(values).containsEntry("1004", 12.36);
			
	}

	// A 2.0 GME freeze frame as read from the car: 11 DIDs. giulia_2.0_gme.json defines 1812 and 1004 with 2 bytes
	// (their live 22 replies), the freeze frame stores 1 byte each, and 181D / 1862 are not live PIDs there at all.
	private static final String GME_SNAPSHOT = "0310:59040121148F1:000B10080001712:1210090000200A3:352860821410004:0000181D10AB105:030B19350B18626:FD9E18120010047:83";

	@Test
	public void snapshotDefinitionsWinOverLivedata() {
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json", "dtc_snapshot.json");
		final UdsSnapshotResponse result = new UdsSnapshotParser(registry, "JavaScript").parse(GME_SNAPSHOT);

		Assertions.assertThat(result.getErrorMessage()).isEmpty();
		Assertions.assertThat(result.getExtractedDids()).hasSize(11);

		final Map<String, Number> values = new HashMap<String, Number>();
		result.getExtractedDids().forEach(p -> values.put(p.getDefinition().getPid(), p.getDecodedValue()));

		Assertions.assertThat(values).containsEntry("181D", 100.01);
		Assertions.assertThat(values).containsEntry("1812", 0.0);
		Assertions.assertThat(values).containsEntry("1004", 12.36);
		Assertions.assertThat(result.getExtractedDids().stream().filter(p -> p.getDefinition().getPid().equals("1004"))
				.findFirst().get().getDefinition().getGroup()).isEqualTo(PIDsGroup.DTC_SNAPSHOT);
	}

	@Test
	public void snapshotDefinitionsAreNotLivedata() {
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json", "dtc_snapshot.json");

		Assertions.assertThat(registry.findAll().stream().map(PidDefinition::getId).collect(Collectors.toSet()))
				.doesNotContain(900001L, 900002L, 900003L, 900004L);
		Assertions.assertThat(registry.findBy(PIDsGroup.DTC_SNAPSHOT)).hasSize(4);
	}

	@Test
	public void codecKeepsTheDidsDecodedBeforeAnUnknownDid() {
		// No dtc_snapshot.json: extraction stops at 181D, after five DIDs. The codec used to return null, so the
		// handler never received the reply and waited for its full timeout.
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final DiagnosticTroubleCodeSnapshotCodec codec = new DiagnosticTroubleCodeSnapshotCodec(
				FormulaEvaluatorConfig.builder().scriptEngine("JavaScript").build(), registry);

		final UdsSnapshotResponse result = codec.decode(null, ConnectorResponseFactory.wrap(GME_SNAPSHOT.getBytes()));

		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(result.getErrorMessage()).contains("181D");
		Assertions.assertThat(result.getDtcHex()).isEqualTo("012114");
		Assertions.assertThat(result.getExtractedDids()).hasSize(5);
	}
}
