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
package org.obd.metrics.codec.batch.decoder;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.obd.metrics.transport.message.ConnectorResponse;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class IdGeneratorTest {

	@Test
	public void arrayIndexOutOfBoundException_Test() {
		ConnectorResponse bytes = ConnectorResponseFactory.wrap("FF".getBytes());
		
		long c = IdGenerator.generate(1, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(170770L);

		c = IdGenerator.generate(2, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(170770L);

		c = IdGenerator.generate(3, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(170770L);

		c = IdGenerator.generate(4, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(170770L);

	}

	@Test
	public void generateId_1Test() {
		ConnectorResponse bytes = ConnectorResponseFactory.wrap( "FFFFFFFF".getBytes());
		
		long c = IdGenerator.generate(1, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(170770L);

		c = IdGenerator.generate(2, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(1777770);

		c = IdGenerator.generate(3, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(177777770);

		c = IdGenerator.generate(4, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(17777777770L);

	}

	@Test
	public void generateId_2Test() {
		ConnectorResponse bytes = ConnectorResponseFactory.wrap( "00FFDC".getBytes());

		long c = IdGenerator.generate(1, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(170528);

		c = IdGenerator.generate(2, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(1753570);

		c = IdGenerator.generate(3, 17L, 0, bytes);
		Assertions.assertThat(c).isEqualTo(175357747);

	}

	@Disabled
	@Test
	public void cacheTest() {
		ConnectorResponse bytes = ConnectorResponseFactory.wrap( "00FFDC".getBytes());

		int iterationHit = 0;
		for (int counter = 0; counter < 900000; counter++) {

			long executionTime = System.nanoTime();
			IdGenerator.generate(1, 17L, 0, bytes);

			IdGenerator.generate(2, 17L, 0, bytes);

			IdGenerator.generate(3, 17L, 0, bytes);
			executionTime = System.nanoTime() - executionTime;
			if (executionTime <= 200) {
				iterationHit = counter;
				break;
			}
		}
		Assertions.assertThat(iterationHit).isBetween(1, 1000);
	}
}
