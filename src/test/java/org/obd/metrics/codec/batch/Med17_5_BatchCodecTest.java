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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.test.PIDsRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Med17_5_BatchCodecTest {
	
	@Test
	public void stn_test() {
		
		final Logger logger = (Logger) LoggerFactory.getLogger("org.obd.metrics.codec.batch.AbstractBatchCodec");
		logger.setLevel(Level.TRACE);
		
		final PIDsRegistry registry = PIDsRegistryFactory.get("mode01.json", "mode01_2.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("15")));
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("04")));
		commands.add(new ObdCommand(registry.findBy("11")));
		final Adjustments optional = Adjustments
		        .builder()
		        .stNxx(STNxxExtensions.builder().enabled(Boolean.TRUE).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(1)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .cachePolicy(CachePolicy.builder().resultCacheEnabled(false).build())
		        .batchPolicy(BatchPolicy
		        		.builder()
		        		 .responseLengthEnabled(Boolean.FALSE)
		        		.enabled(Boolean.TRUE).build())
		        .build();
		
		final byte[] message = "00D0:41155AFF0BFF1:0C000004001100".getBytes();
		final BatchCodec codec = BatchCodec.builder().adjustments(optional).commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

		final ConnectorResponse batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("11")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("15")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
	}
	
	
	@Test
	public void case_01() {
		final PIDsRegistry registry = PIDsRegistryFactory.get("mode01.json", "mode01_2.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("04")));
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("06")));
		final byte[] message = "00F0:41010007E1001:030000040005002:0680AAAAAAAAAA".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

		final ConnectorResponse batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("06")), batchMessage);
	}

	@Test
	public void case_02() {
		final PIDsRegistry registry = PIDsRegistryFactory.get("mode01.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("0F")));
		commands.add(new ObdCommand(registry.findBy("11")));
		final byte[] message = "00C0:4105000BFF0C1:00000F001100AA".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

		final ConnectorResponse batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0B")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0F")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("11")), batchMessage);
	}

	@Test
	public void case_03() {
		final PIDsRegistry registry = PIDsRegistryFactory.get("mode01.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("0C")));
		final byte[] message = "4105000C0000".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

		final ConnectorResponse batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("0C")), batchMessage);
	}

	@Test
	public void case_04() {
		final PIDsRegistry registry = PIDsRegistryFactory.get("mode01.json", "mode01_2.json");
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("04")));
		commands.add(new ObdCommand(registry.findBy("05")));
		commands.add(new ObdCommand(registry.findBy("06")));
		commands.add(new ObdCommand(registry.findBy("07")));

		final byte[] message = "0110:41010007E1001:030000040005002:0680078BAAAAAA".getBytes();
		final BatchCodec codec = BatchCodec.builder().commands(commands).build();
		final Map<ObdCommand, ConnectorResponse> values = codec.decode(ConnectorResponseFactory.wrap(message));

		final ConnectorResponse batchMessage = instance(message);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("04")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("05")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("06")), batchMessage);
		Assertions.assertThat(values).containsEntry(new ObdCommand(registry.findBy("07")), batchMessage);
	}
}
