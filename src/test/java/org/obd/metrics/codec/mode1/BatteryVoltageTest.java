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
package org.obd.metrics.codec.mode1;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.codec.Codec;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class BatteryVoltageTest implements Mode01Test {
	@Test
	public void case_01() {
		final CodecRegistry codecRegistry = CodecRegistry
				.builder()
				.formulaEvaluatorConfig(FormulaEvaluatorConfig
						.builder()
						.scriptEngine("JavaScript").build()).build();

		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("extra.json");

		final PidDefinition pidDef = pidRegistry.findBy(9000l);
		Assertions.assertThat(pidDef).isNotNull();
		Codec<?> codec = codecRegistry.findCodec(pidDef);
		Object value = codec.decode(pidDef, ConnectorResponseFactory.wrap("13.4v".getBytes()));

		Assertions.assertThat(value).isEqualTo(13.4);
	}
}
