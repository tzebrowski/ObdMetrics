/** 
 * Copyright 2019-2023, Tomasz Żebrowski
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.obd.metrics.codec.batch;

import static org.obd.metrics.codec.batch.mapper.BatchMessageBuilder.instance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PIDsRegistry;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Giulia_2_0_GME_MultiAnswerCodecTest {


	@Test
	public void case_01() {
		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("0C", 0);
		expectedValues.put("11", 18);
		expectedValues.put("0E", 0.0);

		final String query = "0C 11 0E";
		final String a1 = "0080:410C0000112D410C00001:0E800000000000";
		final String a2 = "0080:410C0000112D1:0E800000000000410C0000";
		
		runTest(expectedValues, query, Arrays.asList(a1, a2));
	}

	@Test
	public void case_02() {
		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("15", 17.44);
		expectedValues.put("04", 0.0);
		expectedValues.put("06", 0.0);
		expectedValues.put("11", 18);
		expectedValues.put("0E", 0.0);

		final String query = "15 04 06 11 0E";
		final String a1 = "00C0:4115078004001:0680112D0E8000410400";
		final String a2 = "00C0:4115078004004104001:0680112D0E8000";
		
		runTest(expectedValues, query, Arrays.asList(a1, a2, a1, a2, a1, a2));
	}
	
	private void runTest(final Map<String, Object> expectedValues, final String query, List<String> messages) {
		final PIDsRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json", "mode01.json");

		final List<ObdCommand> commands = Arrays.asList(query.split(" ")).stream()
				.map(pid -> new ObdCommand(registry.findBy(pid))).collect(Collectors.toList());
		final BatchCodec codec = BatchCodec.builder().query(query).commands(commands).build();

		for (final String mm : messages) {
			final byte[] message = mm.getBytes();
			final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

			final ConnectorResponse connectorResponse = instance(message);

			Assertions.assertThat(values).isNotEmpty();
			Assertions.assertThat(values).hasSize(commands.size());

			for (final ObdCommand cmd : commands) {
				Assertions.assertThat(values).containsEntry(cmd, connectorResponse);
			}

			final CodecRegistry codecRegistry = CodecRegistry.builder().adjustments(Adjustments.DEFAULT)
					.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().debug(true).build()).build();

			commands.forEach(c -> {
				final ConnectorResponse cr = values.get(c);
				final Object value = codecRegistry.findCodec(c.getPid()).decode(c.getPid(), cr);
				final String pid = c.getPid().getPid();
				final Object expected = expectedValues.get(pid);

				log.debug("PID={}, expected={}, evaluated={},mapping={}", pid, expected, value, cr);

				Assertions.assertThat(value)
						.overridingErrorMessage("PID: %s, expected: %s, evaluated=%s", pid, expected, value)
						.isEqualTo(expected);
			});
		}
	}
}
