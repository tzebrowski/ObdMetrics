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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.test.PIDsRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class IdGeneratorTest {

	/**
	 * Helper method to dynamically calculate the expected bitwise hash.
	 * This mirrors the logic in the new bitwise IdGenerator.
	 */
	private long expectedHash(long pidId, String payload) {
		long hash = pidId << 32;
		for (char c : payload.toCharArray()) {
			hash = (hash << 8) | (c & 0xFF);
		}
		return hash;
	}

	@Test
	public void arrayIndexOutOfBoundException_Test() {
		ConnectorResponse bytes = ConnectorResponseFactory.wrap("FF".getBytes());
		
		long expected = expectedHash(17L, "FF");
		
		long c = IdGenerator.generate(1, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(expected);

		c = IdGenerator.generate(2, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(expected);

		c = IdGenerator.generate(3, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(expected);

		c = IdGenerator.generate(4, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(expected);
	}

	@Test
	public void generateId_1Test() {
		ConnectorResponse bytes = ConnectorResponseFactory.wrap("FFFFFFFF".getBytes());
		
		long c = IdGenerator.generate(1, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(expectedHash(17L, "FF"));

		c = IdGenerator.generate(2, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(expectedHash(17L, "FFFF"));

		c = IdGenerator.generate(3, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(expectedHash(17L, "FFFFFF"));

		c = IdGenerator.generate(4, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(expectedHash(17L, "FFFFFFFF"));
	}

	@Test
	public void generateId_2Test() {
		ConnectorResponse bytes = ConnectorResponseFactory.wrap("00FFDC".getBytes());

		long c = IdGenerator.generate(1, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(expectedHash(17L, "00"));

		c = IdGenerator.generate(2, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(expectedHash(17L, "00FF"));

		c = IdGenerator.generate(3, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(expectedHash(17L, "00FFDC"));
	}

	@Test
	public void shouldSkipMultiFrameColonDelimiters() {
		ConnectorResponse singleFrame = ConnectorResponseFactory.wrap("00FFDC".getBytes());
		ConnectorResponse multiFrame = ConnectorResponseFactory.wrap("001:FFDC".getBytes());

		long singleFrameId = IdGenerator.generate(3, 17L, 0, singleFrame);
		long multiFrameId = IdGenerator.generate(3, 17L, 0, multiFrame);

		Assertions.assertThat(multiFrameId).isEqualTo(singleFrameId);
		Assertions.assertThat(multiFrameId).isEqualTo(expectedHash(17L, "00FFDC"));
	}

	@Test
	public void shouldGenerateCorrectIdsForLargeMultiFrameMessage() {
		final PIDsRegistry registry = PIDsRegistryFactory.get("alfa.json");
		
		String payload = "7F227804E0:6210000000191:240000186B78182:27A15D182825A73:1937A15D181F634:B0180E000018675:2CF7186C00186D6:00186E00186F007:1002000018AD008:0018AE336018C79:3318AF000018C8A:03191008981911B:0898";
		ConnectorResponse bytes = ConnectorResponseFactory.wrap(payload.getBytes());

		// Standard frame extractions using dynamic registry lookups
		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("1000").getId(), 17, bytes)).isEqualTo(expectedHash(registry.findBy("1000").getId(), "0000"));
		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("1924").getId(), 27, bytes)).isEqualTo(expectedHash(registry.findBy("1924").getId(), "0000"));
		Assertions.assertThat(IdGenerator.generate(1, registry.findBy("186B").getId(), 35, bytes)).isEqualTo(expectedHash(registry.findBy("186B").getId(), "78"));
		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("1827").getId(), 45, bytes)).isEqualTo(expectedHash(registry.findBy("1827").getId(), "5D18"));

		// EDGE CASE 1: Multi-frame delimiter (3:) falls squarely in the middle of this read range!
		Assertions.assertThat(IdGenerator.generate(3, registry.findBy("1828").getId(), 53, bytes)).isEqualTo(expectedHash(registry.findBy("1828").getId(), "A71937"));

		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("1937").getId(), 63, bytes)).isEqualTo(expectedHash(registry.findBy("1937").getId(), "5D18"));
		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("181F").getId(), 73, bytes)).isEqualTo(expectedHash(registry.findBy("181F").getId(), "B018"));
		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("180E").getId(), 81, bytes)).isEqualTo(expectedHash(registry.findBy("180E").getId(), "0018"));
		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("1867").getId(), 91, bytes)).isEqualTo(expectedHash(registry.findBy("1867").getId(), "F718"));
		Assertions.assertThat(IdGenerator.generate(1, registry.findBy("186C").getId(), 99, bytes)).isEqualTo(expectedHash(registry.findBy("186C").getId(), "18"));
		Assertions.assertThat(IdGenerator.generate(1, registry.findBy("186D").getId(), 109, bytes)).isEqualTo(expectedHash(registry.findBy("186D").getId(), "6E"));
		Assertions.assertThat(IdGenerator.generate(1, registry.findBy("186E").getId(), 117, bytes)).isEqualTo(expectedHash(registry.findBy("186E").getId(), "00"));
		
		// Corrected drift indices from 186F onwards
		Assertions.assertThat(IdGenerator.generate(1, registry.findBy("186F").getId(), 121, bytes)).isEqualTo(expectedHash(registry.findBy("186F").getId(), "10"));
		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("1002").getId(), 127, bytes)).isEqualTo(expectedHash(registry.findBy("1002").getId(), "0018"));
		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("18AD").getId(), 137, bytes)).isEqualTo(expectedHash(registry.findBy("18AD").getId(), "0018"));
		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("18AE").getId(), 143, bytes)).isEqualTo(expectedHash(registry.findBy("18AE").getId(), "3360"));
		Assertions.assertThat(IdGenerator.generate(1, registry.findBy("18C7").getId(), 153, bytes)).isEqualTo(expectedHash(registry.findBy("18C7").getId(), "33"));
		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("18AF").getId(), 161, bytes)).isEqualTo(expectedHash(registry.findBy("18AF").getId(), "0018"));
		Assertions.assertThat(IdGenerator.generate(1, registry.findBy("18C8").getId(), 169, bytes)).isEqualTo(expectedHash(registry.findBy("18C8").getId(), "03"));
		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("1910").getId(), 175, bytes)).isEqualTo(expectedHash(registry.findBy("1910").getId(), "0898"));

		// EDGE CASE 2: Multi-frame delimiter (B:) falls exactly on the start position!
		Assertions.assertThat(IdGenerator.generate(2, registry.findBy("1911").getId(), 183, bytes)).isEqualTo(expectedHash(registry.findBy("1911").getId(), "0898"));
	}
}