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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.api.CommandsSuplier;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;


public class QuereySizeTest {
	
	@Test
	public void non01PidsTest() {
			final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("rfhub_module.json");
			final Query query = Query.builder()
					.pid(pidRegistry.findBy(154l).getId())
					.pid(pidRegistry.findBy(155l).getId())
					.pid(pidRegistry.findBy(156l).getId())
			        .pid(pidRegistry.findBy(157l).getId())
			        .build();
			
			Adjustments extra = Adjustments.builder()
					.stNxx(STNxxExtensions.builder()
							.enabled(Boolean.FALSE)
							.build())
					.batchPolicy(BatchPolicy.builder()
							.otherModesBatchSize(2)
							.enabled(Boolean.TRUE)
							.responseLengthEnabled(false)
							.build())
					.build();

			final Init init = Init.builder()
					.header(Header.builder().header("DAC7F1").mode("666").build())
					.delayAfterInit(0)
			        .protocol(Protocol.AUTO)
			        .sequence(DefaultCommandGroup.INIT)
			        .build();
			
			List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra, query, init).get();
			
			Assertions.assertThat(collection).isNotEmpty().hasSize(2);
			Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("22 31D0 31D1 ");
			Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("22 31D3 31D2 ");
			
	}
	
	@Test
	public void non01PidsV2Test() {
			final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("rfhub_module.json","giulia_2.0_gme.json");
			final Query query = Query.builder()
					.pid(pidRegistry.findBy(7036l).getId())
					.pid(pidRegistry.findBy(154l).getId())
					.pid(pidRegistry.findBy(155l).getId())
					.pid(pidRegistry.findBy(156l).getId())
			        .pid(pidRegistry.findBy(157l).getId())
			        .build();
			
			Adjustments extra = Adjustments.builder()
					.stNxx(STNxxExtensions.builder()
							.enabled(Boolean.FALSE)
							.build())
					.batchPolicy(BatchPolicy.builder()
							.otherModesBatchSize(2)
							.enabled(Boolean.TRUE)
							.responseLengthEnabled(false)
							.build())
					.build();

			final Init init = Init.builder()
					.header(Header.builder().header("DAC7F1").mode("666").build())
					.delayAfterInit(0)
			        .protocol(Protocol.AUTO)
			        .sequence(DefaultCommandGroup.INIT)
			        .build();
			
			List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra, query, init).get();
			
			Assertions.assertThat(collection).isNotEmpty().hasSize(3);
			Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("22 18F0 ");
			Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("22 31D0 31D1 ");
			Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("22 31D3 31D2 ");
	}
}
