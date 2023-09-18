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
package org.obd.metrics.codec.giulia_2_0_gme;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.command.SupportedPIDsCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class Giulia_2_0_GME_SupportedPidsCommandTest {

	@Test
	public void goup00() {
		final String rawData = "4100BE3DA813410098180001";
		final PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("mode01.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(21000l);
		final SupportedPIDsCommand codec = new SupportedPIDsCommand(pid);
		final List<String> result = codec.decode(pid, ConnectorResponseFactory.wrap(rawData.getBytes()));

		Assertions.assertThat(result).isNotNull().isNotEmpty().containsExactly("01", "03", "04", "05", "06", 
				"07", "0b", "0c", "0d", "0e", "10", "11", "13","15","1c","1f","20","22", "28", "31", "34", "35");

	}

	@Test
	public void goup20() {
		final String rawData = "4120801FB011412080018001";
		final PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("mode01.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(21001l);
		final SupportedPIDsCommand codec = new SupportedPIDsCommand(pid);
		final List<String> result = codec.decode(pid, ConnectorResponseFactory.wrap(rawData.getBytes()));

		Assertions.assertThat(result).isNotNull().isNotEmpty().containsExactly("01", "0c", "0d", "0e", 
				"0f", "10", "11", "13","14","1c","20","22", "28", "2b", "31");


	}

	@Test
	public void goup40() {
		final String rawData = "4140FED09081414040800000";
		final PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("mode01.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(21002l);
		final SupportedPIDsCommand codec = new SupportedPIDsCommand(pid);
		final List<String> result = codec.decode(pid, ConnectorResponseFactory.wrap(rawData.getBytes()));

		Assertions.assertThat(result).isNotNull().isNotEmpty();

	}

	@Test
	public void goup60() {
		final String rawData = "416001214000";
		final PidDefinitionRegistry pidDefinitionRegistry = PIDsRegistryFactory.get("mode01.json");
		final PidDefinition pid = pidDefinitionRegistry.findBy(21003l);
		final SupportedPIDsCommand codec = new SupportedPIDsCommand(pid);
		final List<String> result = codec.decode(pid, ConnectorResponseFactory.wrap(rawData.getBytes()));

		Assertions.assertThat(result).isNotEmpty();
	}
}
