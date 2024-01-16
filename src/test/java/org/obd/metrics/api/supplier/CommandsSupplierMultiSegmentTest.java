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
package org.obd.metrics.api.supplier;

import java.util.List;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.api.CommandsSuplier;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.DiagnosticRequestID;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;

public class CommandsSupplierMultiSegmentTest {
	
	@Test
	public void batchOffTest() {
		final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json", "driveline_coupling.json");
		final Query query = Query.builder()
				.pid(58l) 
		        .pid(59l) 
		        .pid(60l) 
		        .build();
		
		final Adjustments extra = Adjustments.builder()
				.stNxx(STNxxExtensions.builder()
				.enabled(Boolean.FALSE).build())
				.batchPolicy(BatchPolicy.builder()
						.responseLengthEnabled(true)
						.enabled(Boolean.FALSE).build())
				.build();
		
		final Init init = Init.builder()
				.dri(DiagnosticRequestID.builder().key("22").value("DA10F1").build())
				.dri(DiagnosticRequestID.builder().key("01").value("DB33F1").build())
				.dri(DiagnosticRequestID.builder().key("556").value("DA1AF1").build())
				.delayAfterInit(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();

		
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query, init);
		final List<ObdCommand> collection = commandsSupplier.get();
	
		Assertions.assertThat(collection).isNotEmpty().hasSize(3);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("22 0119 2");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("22 010A 2");
		Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("22 010B 2");
	}
	
	@Test
	public void batchOnTest() {
		final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json", "driveline_coupling.json");
		final Query query = Query.builder()
				.pid(58l) 
		        .pid(59l) 
		        .pid(60l) 
		        .build();
		
		final Adjustments extra = Adjustments.builder()
				.stNxx(STNxxExtensions.builder()
				.enabled(Boolean.FALSE).build())
				.batchPolicy(BatchPolicy.builder()
						.responseLengthEnabled(true)
						.enabled(Boolean.TRUE).build())
				.build();
		
		final Init init = Init.builder()
				.dri(DiagnosticRequestID.builder().key("22").value("DA10F1").build())
				.dri(DiagnosticRequestID.builder().key("01").value("DB33F1").build())
				.dri(DiagnosticRequestID.builder().key("556").value("DA1AF1").build())
				.delayAfterInit(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();

		
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query, init);
		final List<ObdCommand> collection = commandsSupplier.get();
	
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("22 0119 010A 010B 5");
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"false=22 010B 2",
			"true=22 010B 2",
		}, delimiter = '=')
	public void virtualPIDsTest(boolean batch, String expected) {
		final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json", "drive_control_module.json");
		final Query query = Query.builder()
				.pid(50l) 
		        .pid(51l) 
		        .pid(52l)
		        .pid(53l)
		        .build();
		
		final Adjustments extra = Adjustments.builder()
				.stNxx(STNxxExtensions.builder()
				.enabled(Boolean.FALSE).build())
				.batchPolicy(BatchPolicy.builder()
						.responseLengthEnabled(true)
						.enabled(batch).build())
				.build();
		
		final Init init = Init.builder()
				.dri(DiagnosticRequestID.builder().key("22").value("DA10F1").build())
				.dri(DiagnosticRequestID.builder().key("01").value("DB33F1").build())
				.dri(DiagnosticRequestID.builder().key("556").value("DA1AF1").build())
				.delayAfterInit(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();
		
		final Supplier<List<ObdCommand>> commandsSupplier = new CommandsSuplier(pidRegistry, extra, query, init);
		final List<ObdCommand> collection = commandsSupplier.get();
	
		Assertions.assertThat(collection).isNotEmpty().hasSize(1);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo(expected);
	}
}
