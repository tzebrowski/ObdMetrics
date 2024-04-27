/** 
 * Copyright 2019-2024, Tomasz Żebrowski
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.obd.metrics.PIDsRegistry;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class CodecTestRunner {

	static enum ValidationStrategy {
		DEFAULT, INVALID_DATA
	}

	@RequiredArgsConstructor
	static class ValidationInput {
		@Getter
		private final Map<String, Object> expectedValues;

		@Getter
		private final String message;

		@Getter
		private ValidationStrategy strategy = ValidationStrategy.DEFAULT;

		public ValidationInput(Map<String, Object> expectedValues, String message, ValidationStrategy strategy) {
			this.expectedValues = expectedValues;
			this.message = message;
			this.strategy = strategy;
		}

	}

	protected void runTest(final String query, List<ValidationInput> input) {
		runTest(query, input, Adjustments.DEFAULT, "giulia_2.0_gme.json", "mode01.json");
	}

	protected void runTest(final String query, List<ValidationInput> input, Adjustments adjustments,
			String... resource) {
		final CodecRegistry codecRegistry = CodecRegistry.builder().adjustments(adjustments)
				.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().debug(true).build()).build();

		final PIDsRegistry registry = PIDsRegistryFactory.get(resource);

		final List<ObdCommand> commands = Arrays.asList(query.split(" ")).stream()
				.filter(id -> registry.findBy(id) != null).map(pid -> new ObdCommand(registry.findBy(pid)))
				.collect(Collectors.toList());
		final BatchCodec codec = BatchCodec.builder().query(query).adjustments(adjustments).commands(commands).build();

		for (final ValidationInput validationInput : input) {

			final byte[] messageBytes = validationInput.getMessage().getBytes();
			final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(messageBytes));

			final ConnectorResponse connectorResponse = instance(messageBytes);

			if (validationInput.getStrategy() == ValidationStrategy.DEFAULT) {
				Assertions.assertThat(values).isNotEmpty();
				Assertions.assertThat(values).hasSize(commands.size());

				for (final ObdCommand cmd : commands) {
					Assertions.assertThat(values).containsEntry(cmd, connectorResponse);
				}

				commands.forEach(c -> {
					final ConnectorResponse cr = values.get(c);
					final Object value = codecRegistry.findCodec(c.getPid()).decode(c.getPid(), cr);
					final String pid = c.getPid().getPid();
					final Object expected = validationInput.getExpectedValues().get(pid);
					if (expected != null) {
						log.debug("PID={}, expected={}, evaluated={},mapping={}", pid, expected, value, cr);

						Assertions.assertThat(value)
								.overridingErrorMessage("PID: %s, expected: %s, evaluated=%s", pid, expected, value)
								.isEqualTo(expected);
					}
				});
			} else {
				Assertions.assertThat(values).isEmpty();
			}
		}
	}
}
