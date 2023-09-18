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

import java.io.IOException;
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

public class BatchCodecCacheTest {

	@Test
	public void cacheHitTest() throws IOException {
		PIDsRegistry registry = PIDsRegistryFactory.get("mode01.json");

		List<ObdCommand> commands = new ArrayList<>();
		commands.add(new ObdCommand(registry.findBy("0C")));
		commands.add(new ObdCommand(registry.findBy("10")));
		commands.add(new ObdCommand(registry.findBy("0B")));
		commands.add(new ObdCommand(registry.findBy("0D")));
		commands.add(new ObdCommand(registry.findBy("05")));
		final String message = "00B0:410C000010001:000B660D000000";
		final BatchCodec codec = BatchCodec.builder().commands(commands).query(message).build();

		int len = 10;
		for (int i = 0; i < len; i++) {
			final Map<ObdCommand, ConnectorResponse> values = codec
					.decode(ConnectorResponseFactory.wrap(message.getBytes()));
			Assertions.assertThat(values).containsKey(new ObdCommand(registry.findBy("0B")));
			Assertions.assertThat(values).containsKey(new ObdCommand(registry.findBy("0C")));
			Assertions.assertThat(values).containsKey(new ObdCommand(registry.findBy("0D")));
			Assertions.assertThat(values).containsKey(new ObdCommand(registry.findBy("10")));
		}

		Assertions.assertThat(codec.getCacheHit(message)).isEqualTo(len - 1);

	}
}
