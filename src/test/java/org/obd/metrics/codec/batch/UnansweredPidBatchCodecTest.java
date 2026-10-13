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
package org.obd.metrics.codec.batch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.test.PIDsRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

// An ECU answers only the PIDs it supports. A PID left unanswered must stay unmapped, not be found in the reply's
// answer code: the 2.0 GME answers "01 01 41 31" with "410188077171" (01 01 only), and PID 41 matched the "41".
public class UnansweredPidBatchCodecTest extends BatchCodecTestRunner {

	@Test
	public void unansweredPidIsNotMappedOntoTheAnswerCode() {
		final Map<String, String> values = decode("01 01 41 31", "410188077171");

		Assertions.assertThat(values).containsOnlyKeys("01");
		Assertions.assertThat(values.get("01")).isEqualTo("88077171");
	}

	@Test
	public void answeredPidsAfterAnUnansweredOneAreStillMapped() {
		final Map<String, String> values = decode("01 4E 21 4D", "41210000");

		Assertions.assertThat(values).containsOnlyKeys("21");
		Assertions.assertThat(values.get("21")).isEqualTo("0000");
	}

	private Map<String, String> decode(final String query, final String answer) {
		final PIDsRegistry registry = PIDsRegistryFactory.get("mode01.json", "mode01_2.json");
		final List<ObdCommand> commands = Arrays.asList(query.split(" ")).stream().skip(1)
				.map(pid -> new ObdCommand(registry.findBy(pid))).collect(Collectors.toList());
		final BatchCodec codec = BatchCodec.builder().query(query).adjustments(ADJUSTEMENTS).commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> mapping = codec.decode(query, commands,
				ConnectorResponseFactory.wrap(answer.getBytes()));
		return mapping.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getPid().getPid(),
				e -> e.getValue().getRawValue(e.getKey().getPid())));
	}
}
