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
package org.obd.metrics.codec;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.codec.batch.BatchCodec;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public interface CodecTest {

	default void assertEquals(String pid, String pidSource, String rawData, Object expectedValue) {
		assertEquals(false, pid, null, pidSource, rawData, expectedValue);
	}

	default void assertEquals(boolean debug, String pid, Long id, String pidSource, String rawData,
			Object expectedValue) {

		Assertions.assertThat(pid).isNotNull();
		Assertions.assertThat(pidSource).isNotNull();
		Assertions.assertThat(rawData).isNotNull();

		final CodecRegistry codecRegistry = CodecRegistry.builder().formulaEvaluatorConfig(
				FormulaEvaluatorConfig.builder().debug(debug).scriptEngine("JavaScript").build()).build();

		PidDefinition pidDef = null;
		if (id == null) {
			pidDef = PIDsRegistryFactory.get(pidSource).findBy(pid);
		} else {
			pidDef = PIDsRegistryFactory.get(pidSource).findBy(id);
		}

		Assertions.assertThat(pidDef).isNotNull();
		final Codec<?> codec = codecRegistry.findCodec(pidDef);
		
		if (codec == null) {
			Assertions.fail("No codec available for PID: {}", pid);
		} else {
			if (pidDef.getOverrides().isMultiSegmentAnswer()) {
				ObdCommand command = new ObdCommand(pidDef);
				
				final List<ObdCommand> commands = Arrays.asList(command);
				final BatchCodec batchCodec = BatchCodec.builder().query(pidDef.getPid()).commands(commands).build();
				final Map<ObdCommand, ConnectorResponse> values = batchCodec.decode(ConnectorResponseFactory.wrap(rawData.getBytes()));
				
				final ConnectorResponse cr = values.get(command);
				final Object value = codecRegistry.findCodec(command.getPid()).decode(command.getPid(), cr);
				final Object expected = expectedValue;
				Assertions.assertThat(value)
							.overridingErrorMessage("PID: %s, expected: %s, evaluated=%s", pid, expected, value)
							.isEqualTo(expected);

			} else { 
				final Object actualValue = codec.decode(pidDef, ConnectorResponseFactory.wrap(rawData.getBytes()));
				Assertions.assertThat(actualValue).isEqualTo(expectedValue);
			}
		}
	}

	default void assertCloseTo(boolean debug, String pid, String pidSource, String rawData, float expectedValue,
			float offset) {

		Assertions.assertThat(pid).isNotNull();
		Assertions.assertThat(pidSource).isNotNull();
		Assertions.assertThat(rawData).isNotNull();

		final CodecRegistry codecRegistry = CodecRegistry.builder().formulaEvaluatorConfig(
				FormulaEvaluatorConfig.builder().debug(debug).scriptEngine("JavaScript").build()).build();

		final PidDefinition pidDef = PIDsRegistryFactory.get(pidSource).findBy(pid);
		Assertions.assertThat(pidDef).isNotNull();
		final Codec<?> codec = codecRegistry.findCodec(pidDef);

		if (codec == null) {
			Assertions.fail("No codec available for PID: {}", pid);
		} else {
			final Float actualValue = ((Number) codec.decode(pidDef, ConnectorResponseFactory.wrap(rawData.getBytes()))).floatValue();
			Assertions.assertThat(actualValue).isCloseTo(expectedValue, Offset.offset(offset));
		}
	}
}
