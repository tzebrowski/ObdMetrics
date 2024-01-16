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
package org.obd.metrics;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.Resource;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class ObdMetricsEqualsTest {

	@Test
	void t0() {

		final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("mode01.json");
		PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(
				Resource.builder().inputStream(source).name("mode01.json").build()).build();

		List<ObdMetric> metrics = new ArrayList<>();
		metrics.add(
		        ObdMetric.builder().command(new ObdCommand(pidRegistry.findBy(6l))).raw(ConnectorResponseFactory.wrap("410522".getBytes())).value(-6).build());

		metrics.add(
		        ObdMetric.builder().command(new ObdCommand(pidRegistry.findBy(16l))).raw(ConnectorResponseFactory.wrap("410f2f".getBytes())).value(7).build());

		ObdMetric coolant = ObdMetric.builder().command(new ObdCommand(pidRegistry.findBy(6l))).raw(ConnectorResponseFactory.wrap("410517".getBytes())).value(-17)
		        .build();

		Assertions.assertThat(metrics.indexOf(coolant)).isEqualTo(0);

	}

	@Test
	void t1() {

		final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream("mode01.json");
		PidDefinitionRegistry pidRegistry = PidDefinitionRegistry.builder().source(
				Resource.builder().inputStream(source).name("mode01.json").build()).build();

		List<ObdMetric> metrics = new LinkedList<>();

		metrics.add(
		        ObdMetric.builder().command(new ObdCommand(pidRegistry.findBy(16l))).raw(ConnectorResponseFactory.wrap("410f2f".getBytes())).value(7).build());
		metrics.add(ObdMetric.builder().command(new ObdCommand(pidRegistry.findBy(22l))).raw(ConnectorResponseFactory.wrap("41175aff".getBytes())).value(0.45)
		        .build());
		metrics.add(
		        ObdMetric.builder().command(new ObdCommand(pidRegistry.findBy(6l))).raw(ConnectorResponseFactory.wrap("410522".getBytes())).value(-6).build());
		metrics.add(ObdMetric.builder().command(new ObdCommand(pidRegistry.findBy(23l))).raw(ConnectorResponseFactory.wrap("41175aff".getBytes())).value(0.45)
		        .build());

		ObdMetric coolant = ObdMetric.builder().command(new ObdCommand(pidRegistry.findBy(23l))).raw(ConnectorResponseFactory.wrap("41175aff".getBytes()))
		        .value(0.45).build();

		Assertions.assertThat(metrics.indexOf(coolant)).isEqualTo(3);

	}
}
