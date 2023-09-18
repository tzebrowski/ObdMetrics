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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PIDsRegistry;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class Med_17_3_Mode22_BatchCodedTest {
	
	@Test
	public void case_01(){
		final PIDsRegistry registry = PIDsRegistryFactory.get("alfa.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("1867")));
		commands.add(new ObdCommand(registry.findBy("180E")));

		final byte[] message = "0090:6218670000181:0E0000".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));


		final ConnectorResponse batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1867")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("180E")), batchMessage);
	}

	@Test
	public void case_02() {
		final PIDsRegistry registry = PIDsRegistryFactory.get("alfa.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("194F")));
		commands.add(new ObdCommand(registry.findBy("1003")));
		commands.add(new ObdCommand(registry.findBy("1935")));

		final byte[] message = "00B0:62194F2E65101:0348193548".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));


		final ConnectorResponse batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("194F")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1003")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("1935")), batchMessage);
	}
}
