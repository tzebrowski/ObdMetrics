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
package org.obd.metrics.api.cache;

import java.util.Collection;

import org.apache.commons.collections4.MultiValuedMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PIDsRegistry;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.api.model.Query;

public class EcuAnswerGeneratorTest {

	@Test
	public void above24CharactersTest() {

		final PIDsRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");

		Query query = Query.builder()
		        .pid(pidRegistry.findBy("0C").getId())
		        .pid(pidRegistry.findBy("10").getId())
		        .pid(pidRegistry.findBy("0B").getId())
		        .pid(pidRegistry.findBy("0D").getId())
		        .pid(pidRegistry.findBy("05").getId())
		        .pid(pidRegistry.findBy("0F").getId())
		        .build();

		final int numberOfEntries = 128 * 128 * 128;
		MultiValuedMap<String, String> answers = new EcuAnswerGenerator().generate(query, numberOfEntries);
		Assertions.assertThat(answers.keys()).isNotEmpty();
		Assertions.assertThat(answers.keys()).contains("01 0C 0B 0D 10 0F 05");
		Collection<String> collection = answers.get("01 0C 0B 0D 10 0F 05");
		Assertions.assertThat(collection).hasSize(numberOfEntries);
		
		for (String ans : collection) {
			Assertions.assertThat(ans)
			        .hasSizeGreaterThanOrEqualTo("00F0:410CFFFE0BFE1:0DFE10FFFE0FFE2:05FE".length());
		}
	}

	
	@Test
	public void below24CharacterTest() {
		Query query = Query.builder()
		        .pid(15l) // Boost
		        .pid(22l) // AFR
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(15l) // Timing advance
		        .build();

		final int numberOfEntries = 128 * 128;

		MultiValuedMap<String, String> answers = new EcuAnswerGenerator().generate(query, numberOfEntries);
		
		Assertions.assertThat(answers.keys()).contains("01 15 0B 0C 11 0D");
		Collection<String> collection = answers.get("01 15 0B 0C 11 0D");
		Assertions.assertThat(collection).hasSize(numberOfEntries);
		
		Assertions.assertThat(answers.keys()).isNotEmpty();
	}
}
