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
package org.obd.metrics;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class ObdMetricTest {

	@Test
	void conversion() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		final PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(-150).build();
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(-150.0);
		Assertions.assertThat(metric.valueToString()).isEqualTo("-150");
		Assertions.assertThat(metric.getValue()).isEqualTo(-150);
	}
	
	
	@Test
	void null_value() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		final PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(null).build();
		Assertions.assertThat(Double.isNaN(metric.valueToDouble())).isTrue();
		Assertions.assertThat(metric.valueToString()).isEqualTo("No data");
		Assertions.assertThat(metric.getValue()).isEqualTo(null);
	}
	
	
	@Test
	void double_value() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		final PidDefinition coolant = pidRegistry.findBy(6l);
		Assertions.assertThat(coolant).isNotNull();

		ObdMetric metric = ObdMetric.builder().command(new ObdCommand(coolant)).value(20.12345d).build();
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(20.12d);
		Assertions.assertThat(metric.valueToString()).isEqualTo("20.12");
		Assertions.assertThat(metric.getValue()).isEqualTo(20.12345d);
	}
}
