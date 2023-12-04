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

import static org.obd.metrics.codec.batch.decoder.BatchMessageBuilder.instance;

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
import org.obd.metrics.codec.formula.FormulaEvaluatorPolicy;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Giulia_2_0_GME_CodecTest {

	@Test
	public void case01() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("1004", 12.1);
		expectedValues.put("181F", 1013);
		expectedValues.put("1937", 1001);
		expectedValues.put("130A", 0.0);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1015);
		expectedValues.put("1935", 21);
		expectedValues.put("1302", 20);
		expectedValues.put("1837", 20);
		expectedValues.put("3A58", 23);
		expectedValues.put("18BA", 535);

		final String query = "181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004";
		final String ecuAnswer = "0270:62181F03F5191:3703E9130A19192:2400195603F7193:353D13020014184:370E3A583F18BA5:7510040079";

		runTest(expectedValues,query, Arrays.asList(ecuAnswer));
	}

	@Test
	public void case02() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("1004", 12.1);
		expectedValues.put("181F", 1006);
		expectedValues.put("1937", 1001);
		expectedValues.put("130A", 0.0);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1015);
		expectedValues.put("1935", 21.0);
		expectedValues.put("1302", 20);
		expectedValues.put("1837", 20);
		expectedValues.put("3A58", 23);
		expectedValues.put("18BA", -135);
		expectedValues.put("1935", 22);

		final String query = "181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA";
		final String ecuAnswer = "0230:62181F03EE191:3703E9130A19192:2400195603F7193:353E13020014184:370E3A583F18BA";

		runTest(expectedValues,query, Arrays.asList(ecuAnswer));

	}

	@Test
	public void case03() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("1004", 12.1);
		expectedValues.put("181F", 1008);
		expectedValues.put("1937", 1001);
		expectedValues.put("130A", 0.04);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1016);
		expectedValues.put("1935", 15);
		expectedValues.put("1302", 15);
		expectedValues.put("1837", 15);
		expectedValues.put("3A58", 15);
		expectedValues.put("18BA", 530);

		final String query = "181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004";
		final String ecuAnswer = "0270:62181F03F0191:3703E9130A1A192:2400195603F8193:35371302000F184:370D3A583718BA5:7410040079";

		runTest(expectedValues,query, Arrays.asList(ecuAnswer));

	}

	@Test
	public void case04() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("181F", 1008);
		expectedValues.put("1937", 1001);
		expectedValues.put("130A", 0.04);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1016);
		expectedValues.put("1935", 15);
		expectedValues.put("1302", 15);
		expectedValues.put("1837", 15);
		expectedValues.put("3A58", 15);
		expectedValues.put("18BA", 530);

		final String query = "181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA";
		final String ecuAnswer = "0230:62181F03F0191:3703E9130A1A192:2400195603F8193:35371302000F184:370D3A583718BA5:74";

		runTest(expectedValues,query, Arrays.asList(ecuAnswer));

	}

	@Test
	public void case05() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("181F", 1007);
		expectedValues.put("1937", 1001);
		expectedValues.put("130A", 0.00);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1016);
		expectedValues.put("1935", 70);
		expectedValues.put("1302", 99);
		expectedValues.put("1837", 300);
		expectedValues.put("3A58", 71);
		expectedValues.put("18BA", 485);
		expectedValues.put("1004", 12.5);

		final String query = "181F 1937 130A 1924 1935 1302 1837 3A58 18BA 1004";
		final String ecuAnswer = "0230:62181F03EF191:3703E9130A19192:240019356E13023:00631837463A584:6F18BA6B1004005:7D";

		runTest(expectedValues,query, Arrays.asList(ecuAnswer));

	}

	@Test
	public void case06() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("181F", 1007);
		expectedValues.put("1937", 1001);
		expectedValues.put("130A", 0.00);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1016);
		expectedValues.put("1935", 70);
		expectedValues.put("1302", 96);
		expectedValues.put("1837", 260);
		expectedValues.put("3A58", 69);
		expectedValues.put("18BA", 485);
		expectedValues.put("1004", 12.4);
		expectedValues.put("1000", 58);

		final String query = "181F 1937 1000 130A 1924 1935 1302 1837 3A58 18BA 1004";
		final String ecuAnswer = "0270:62181F03EF191:3703E9100000002:130A19192400193:356E13020060184:373E3A586D18BA5:6B1004007C";

		runTest(expectedValues,query, Arrays.asList(ecuAnswer));

	}

	@Test
	public void case07() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("181F", 990);
		expectedValues.put("1937", 985);
		expectedValues.put("130A", 0.0);
		expectedValues.put("1924", 0.0);

		final String query = "181F 1937 130A 1924";
		final String ecuAnswer = "00F0:62181F03DE191:3703D9130A19192:2400";

		runTest(expectedValues,query, Arrays.asList(ecuAnswer));

	}

	@Test
	public void case08() {

		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("181F", 990);
		expectedValues.put("1937", 985);
		expectedValues.put("130A", 0.00);
		expectedValues.put("1924", 0.0);

		final String query = "181F 1937 130A 1924";
		final String ecuAnswer = "00F0:62181F03DE191:3703D9130A19192:2400";

		runTest(expectedValues,query, Arrays.asList(ecuAnswer));

	}

	@Test
	public void case_09() {
		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("181F", 996);
		expectedValues.put("1937", 985);
		expectedValues.put("130A", 0.00);
		expectedValues.put("1924", 0.0);
		expectedValues.put("1956", 1016);
		expectedValues.put("1935", 17);
		expectedValues.put("1302", 18);
		expectedValues.put("1837", 275.56);
		expectedValues.put("3A58", 16);
		expectedValues.put("18BA", 470);
		expectedValues.put("1004", 12.2);

		final String query = "181F 1937 130A 1924 1935 1302 3A58 18BA 1004";
		final String ecuAnswer = "0200:62181F03E4191:3703D9130A19192:240019353913023:00123A583818BA4:681004007A";

		runTest(expectedValues,query, Arrays.asList(ecuAnswer));

	}

	@Test
	public void case_10() {
		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("195A", 1004);
		expectedValues.put("1935", 54);
		expectedValues.put("1302", 96);

		final String query = "195A 1935 1302";
		final String ecuAnswer = "00C0:62195A03EC191:355E13020060";

		runTest(expectedValues,query, Arrays.asList(ecuAnswer));

	}

	@Test
	public void case_11() {
		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("15", 15.32);
		expectedValues.put("0C", 0);
		expectedValues.put("04", 0.0);
		expectedValues.put("06", 0.0);
		expectedValues.put("11", 18);
		expectedValues.put("0E", 0.0);

		final String query = "15 0C 04 06 11 0E";
		final String ecuAnswer = "410CFFFF04FF00F0:411553800C001:0004000680112D2:0E800000000000";

		runTest(expectedValues,query, Arrays.asList(ecuAnswer));

	}

	@Test
	public void case_12() {
		final Map<String, Object> expectedValues = new HashMap<>();
		expectedValues.put("15", 17.28);
		expectedValues.put("0C", 0);
		expectedValues.put("04", 0.0);
		expectedValues.put("06", 0.0);
		expectedValues.put("11", 18);
		expectedValues.put("0E", 0.0);

		final String query = "15 0C 04 06 11 0E";
		final String ecuAnswer = "00F0:41150D800C001:0004000680112D2:0E800000000000410C00000400";

		runTest(expectedValues,query, Arrays.asList(ecuAnswer));
	}

	protected void runTest(final Map<String, Object> expectedValues, final String query, List<String> messages) {
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
					.formulaEvaluatorPolicy(FormulaEvaluatorPolicy.builder().debug(true).build()).build();

			commands.forEach(c -> {
				final ConnectorResponse cr = values.get(c);
				final Object value = codecRegistry.findCodec(c.getPid()).decode(c.getPid(), cr);
				final String pid = c.getPid().getPid();
				final Object expected = expectedValues.get(pid);
				if (expected != null) {
					log.debug("PID={}, expected={}, evaluated={},mapping={}", pid, expected, value, cr);
	
					Assertions.assertThat(value)
							.overridingErrorMessage("PID: %s, expected: %s, evaluated=%s", pid, expected, value)
							.isEqualTo(expected);
				}
			});
		}
	}
}
