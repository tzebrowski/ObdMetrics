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
package org.obd.metrics.api;

import java.util.List;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class CommandsSupplierTest {

	@Test
	public void containsOnlyUniquePidsTest() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json");
		final Query query = Query.builder()
				.pid(22l) // O2 Voltage
		        .pid(23l) // AFR
				.pid(12l) // Boost
		        .pid(99l) // Intake pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(15l) // Timing advance
		        .pid(9000l) // Battery voltage
		        .build();
		
		final Adjustments extra = Adjustments
				.builder()
				.batchPolicy(BatchPolicy.builder()
						.responseLengthEnabled(true)
						.enabled(Boolean.TRUE)
				.build())
				.build();
		
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra ,query,Init.DEFAULT);
		final List<ObdCommand> collection = commandsSupplier.get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(3);
		
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("01 15 0B 0C 11 0D 2");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("01 0E 1");
		Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("01 0F 1");
	}
	
	
	@Test
	public void multiModeTest() {
		final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json","alfa.json");
		final Query query = Query.builder()
				.pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(18l) // Throttle position
		        
				.pid(6014l) // mass air flow target
		        .pid(6013l) // mass air flow
		        .pid(6007l) // IAT
		        .pid(6012l) // target manifold pressure
		        .build();
		
		final Adjustments extra = Adjustments.builder()
				.batchPolicy(BatchPolicy.builder()
					.responseLengthEnabled(true)
					.enabled(Boolean.TRUE).build())
				.build();
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query,
				Init.DEFAULT);
		final List<ObdCommand> collection = commandsSupplier.get();
	
		Assertions.assertThat(collection).isNotEmpty().hasSize(3);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("22 1867 180E 181F 2");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("22 1935 1");
		Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("01 0B 0C 11 2");
	}
	
	@Test
	public void lessThanSixPidsTest() {
		final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json","alfa.json");
		final Query query = Query.builder()
				.pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        
			    .pid(6013l) // mass air flow
		        .pid(6007l) // IAT
		        .pid(6012l) // target manifold pressure
		        .build();
		
		final Adjustments extra = Adjustments
				.builder()
				.batchPolicy(BatchPolicy.builder()
					.responseLengthEnabled(true)
					.enabled(Boolean.TRUE)
					.build())
				.build();
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query,
				Init.DEFAULT);
		final List<ObdCommand> collection = commandsSupplier.get();
	
		Assertions.assertThat(collection).isNotEmpty().hasSize(2);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("22 180E 181F 1935 2");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("01 0B 0C 1");
	}
}
