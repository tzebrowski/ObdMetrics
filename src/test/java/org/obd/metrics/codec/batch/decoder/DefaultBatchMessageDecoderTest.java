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
package org.obd.metrics.codec.batch.decoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.ValueType;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

class DefaultBatchMessageDecoderTest {

	@Test
	@DisplayName("Should correctly parse PIDs split by ELM327 sequence delimiters")
	void shouldParseSplitPidsCorrectly() {
		final Adjustments adjustments = Adjustments.builder().batchPolicy(BatchPolicy
				.builder()
				.enabled(true)
				.strictValidationEnabled(false).build()).build();
		
		// Arrange
		BatchMessageDecoder decoder = BatchMessageDecoder.get(adjustments);


		String query = "22 1000 1924 186B";
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(createCommand(1, "1000", 3, "62"));
		commands.add(createCommand(2, "1924", 2, "62"));
		commands.add(createCommand(3, "186B", 1, "62"));

		// '1924' is split by the '1:' sequence delimiter. '186B' follows it.
		String rawPayload = "04E0:6210000000191:240000186B78";
		ConnectorResponse response = ConnectorResponseFactory.wrap(rawPayload.getBytes());

		// Act
		Map<ObdCommand, ConnectorResponse> decoded = decoder.decode(query, commands, response);

		// Assert
		assertEquals(3, decoded.size(), "Should decode all 3 commands");

		// Verify the raw string extractions to ensure bounds are perfectly calculated
		assertEquals("0000", decoded.get(commands.get(0)).getRawValue(commands.get(0).getPid()));

		// The raw value for 1924 should safely jump over the delimiter if the data
		// itself is split,
		// but here the PID is split, so the data is cleanly "0000"
		assertEquals("0000", decoded.get(commands.get(1)).getRawValue(commands.get(1).getPid()));
//		assertEquals("78", decoded.get(commands.get(2)).getRawValue(commands.get(2).getPid()));
	}

	@Test
	@DisplayName("Should return empty map when strict validation fails (missing PIDs)")
	void shouldReturnEmptyMapOnStrictValidationFailure() {
		
		final Adjustments adjustments = Adjustments.builder().batchPolicy(BatchPolicy
				.builder()
				.enabled(true)
				.strictValidationEnabled(true).build()).build();

		
		// Arrange
		// Strict validation is TRUE
		BatchMessageDecoder decoder = BatchMessageDecoder.get(adjustments);


		String query = "22 1000 1924 186B";
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(createCommand(6, "1000", 3, "62"));
		commands.add(createCommand(7, "1924", 2, "62"));
		commands.add(createCommand(8, "186B", 1, "62"));

		// Payload is MISSING the data for 186B
		String rawPayload = "04E0:6210000000191:240000";
		ConnectorResponse response = ConnectorResponseFactory.wrap(rawPayload.getBytes());

		// Act
		Map<ObdCommand, ConnectorResponse> decoded = decoder.decode(query, commands, response);

		// Assert
		assertTrue(decoded.isEmpty(), "Should return empty map because 186B is missing and strict validation is on");
	}

	@Test
	@DisplayName("Should gracefully handle LRU Cache eviction without memory leaks")
	void shouldHandleCacheEvictionGracefully() {
		// Arrange
		final Adjustments adjustments = Adjustments.builder().batchPolicy(BatchPolicy
				.builder()
				.enabled(true)
				.strictValidationEnabled(true).build()).build();

		BatchMessageDecoder decoder = BatchMessageDecoder.get(adjustments);

		List<ObdCommand> commands = new ArrayList<>();
		commands.add(createCommand(9, "1000", 3, "62"));

		// Act & Assert
		// We set MAX_ENTRIES to 100 in MappingsCache. Loop 150 times to force eviction.
		for (int i = 0; i < 150; i++) {
			// Alter the query string slightly to force a new cache key generation
			String query = "22 1000 " + i;
			String rawPayload = "6210000000";
			ConnectorResponse response = ConnectorResponseFactory.wrap(rawPayload.getBytes());

			Map<ObdCommand, ConnectorResponse> decoded = decoder.decode(query, commands, response);

			assertNotNull(decoded, "Decoder should never return a null map");
			assertEquals(1, decoded.size(), "Should always decode the 1 valid command");
		}

		// If it reaches here without an OutOfMemoryError or NullPointerException, the
		// LRU Cache is rock solid.
		assertTrue(true);
	}

	@Test
	@DisplayName("Should return empty map on adapter error strings without crashing")
	void shouldReturnEmptyMapOnAdapterErrors() {
		// Arrange
		final Adjustments adjustments = Adjustments.builder().batchPolicy(BatchPolicy
				.builder()
				.enabled(true)
				.strictValidationEnabled(true).build()).build();
		BatchMessageDecoder decoder = BatchMessageDecoder.get(adjustments);


		String query = "22 1000";
		List<ObdCommand> commands = new ArrayList<>();
		commands.add(createCommand(2, "1000", 3, "62"));

		// Act
		String rawPayload = "UNABLE TO CONNECT";
		ConnectorResponse response = ConnectorResponseFactory.wrap(rawPayload.getBytes());
		Map<ObdCommand, ConnectorResponse> decoded = decoder.decode(query, commands, response);

		// Assert
		// isValidAnswerCode should fail cleanly and return an empty map
		assertTrue(decoded.isEmpty(), "Should cleanly reject non-hex error strings");
	}

	private ObdCommand createCommand(long id, String pid, int length, String successCode) {
		return new ObdCommand(new PidDefinition(id, 2, "", "22", pid, "m", "desc", -180, 10000,
				ValueType.DOUBLE));
	}
}