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
package org.obd.metrics.api;

import java.util.List;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;

public class NumberOfLinesTest {

	@Test
	public void threeLines() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");
		final Query query = Query.builder()
				.pid(13l) // Engine RPM
		        .pid(12l) // Boost
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(5l) // engine load
				.pid(7l)  // Short fuel trim
		        .build();

		final Adjustments extra = Adjustments
				.builder()
				.batchPolicy(BatchPolicy.builder()
					.responseLengthEnabled(true)
					.enabled(Boolean.TRUE).build())
				.build();
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query, Init.DEFAULT);
		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);

		//ends with 3 - means three lines in the response
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("01 0C 0B 11 0D 04 06 3");
	}

	@Test
	public void twoLines() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");
		final Query query = Query.builder()
				.pid(13l) // Engine RPM
		        .pid(12l) // Boost
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(5l) // engine load
		        .build();

		
		final Adjustments extra = Adjustments
				.builder()
				.batchPolicy(BatchPolicy.builder()
					.responseLengthEnabled(true)	
					.enabled(Boolean.TRUE).build())
				.build();
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query,Init.DEFAULT);

		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);
		
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("01 0C 0B 11 0D 04 2");
	}
	
	@Test
	public void twoLines_2() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");
		final Query query = Query.builder()
				.pid(13l) // Engine RPM
		        .pid(12l) // Boost
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		
		final Adjustments extra = Adjustments
				.builder()
				.batchPolicy(BatchPolicy.builder()
					.responseLengthEnabled(true)	
					.enabled(Boolean.TRUE).build())
				.build();
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra,query,Init.DEFAULT);

		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);
		
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("01 0C 0B 11 0D 2");
	}
	
	@Test
	public void twoLines_3() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");
		final Query query = Query.builder()
				.pid(13l) // Engine RPM
		        .pid(12l) // Boost
		        .pid(18l) // Throttle position
		        .build();

		
		final Adjustments extra = Adjustments
				.builder()
				.batchPolicy(BatchPolicy.builder()
					.responseLengthEnabled(true)
					.enabled(Boolean.TRUE).build())
				.build();
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query,Init.DEFAULT);

		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);

		//ends with 2 - means two lines in the response
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("01 0C 0B 11 2");
	}
	
	@Test
	public void oneLine() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");
		final Query query = Query.builder()
				.pid(13l) // Engine RPM
		        .pid(12l) // Boost
		        .build();

		
		final Adjustments extra = Adjustments
				.builder()
				.batchPolicy(BatchPolicy.builder()
					.responseLengthEnabled(true)
					.enabled(Boolean.TRUE).build())
				.build();
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query,Init.DEFAULT);

		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);
		
		//ends with 1 - means one line in the response
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("01 0C 0B 1");
	}
	
	
	@Test
	public void oneLine_v2() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("alfa.json");
		final Query query = Query.builder()
				.pid(6004l) 
		        .pid(6005l) 
		        .build();
//		
//		09:41:45.945 TRACE DefaultConnector - TX: 22 1000 1924 1
//		09:41:46.084 TRACE DefaultConnector - RX: 0090:621000000019, processing time: 139ms
//		
		final Adjustments extra = Adjustments
				.builder()
				.batchPolicy(BatchPolicy.builder()
					.responseLengthEnabled(true)	
					.enabled(Boolean.TRUE).build())
				.build();
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query,Init.DEFAULT);

		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);
		
		//ends with 1 - means one line in the response
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("22 1000 1924 2");
	}
}
