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

//6009, 6012, 6065, 6031, 6030, 6010, 6019

public class STNxxxAggregationTest {
	
	@Test
	public void justP0Test() {
			final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("alfa.json");
			final Query query = Query.builder()
					.pid(pidRegistry.findBy(6009l).getId())
					.pid(pidRegistry.findBy(6012l).getId())
					.pid(pidRegistry.findBy(6065l).getId())
			        .pid(pidRegistry.findBy(6031l).getId())
			        .pid(pidRegistry.findBy(6030l).getId())
			        .pid(pidRegistry.findBy(6010l).getId())
				    .pid(pidRegistry.findBy(6019l).getId())
			        .build();
			
			Adjustments extra = Adjustments.builder()
					.stNxx(STNxxExtensions.builder()
							.enabled(Boolean.TRUE)
							.promoteAllGroupsEnabled(Boolean.TRUE).build())
					.batchPolicy(BatchPolicy.builder()
							.otherModesBatchSize(7)
							.enabled(Boolean.TRUE)
							.responseLengthEnabled(false)
							.build())
					.build();

			final Init init = Init.builder()
					.header(Header.builder().header("18DB33F1").mode("01").build())
					.header(Header.builder().header("18DA10F1").mode("22").build())
					.delayAfterInit(0)
			        .protocol(Protocol.AUTO)
			        .sequence(DefaultCommandGroup.INIT)
			        .build();
			
			List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra, query, init).get();
			
			Assertions.assertThat(collection).isNotEmpty().hasSize(1);
			Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1827 181F 1864 1910 18C8 1828 186F");
	}
	
	@Test
	public void justP0BatchSizeTest() {
			final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("alfa.json");
			final Query query = Query.builder()
					.pid(pidRegistry.findBy(6009l).getId())
					.pid(pidRegistry.findBy(6012l).getId())
					.pid(pidRegistry.findBy(6065l).getId())
			        .pid(pidRegistry.findBy(6031l).getId())
			        .pid(pidRegistry.findBy(6030l).getId())
			        .pid(pidRegistry.findBy(6010l).getId())
				    .pid(pidRegistry.findBy(6019l).getId())
			        .build();
			
			Adjustments extra = Adjustments.builder()
					.stNxx(STNxxExtensions.builder()
							.enabled(Boolean.TRUE)
							.promoteAllGroupsEnabled(Boolean.TRUE).build())
					.batchPolicy(BatchPolicy.builder()
							.otherModesBatchSize(3)
							.enabled(Boolean.TRUE)
							.responseLengthEnabled(false)
							.build())
					.build();

			final Init init = Init.builder()
					.header(Header.builder().header("18DB33F1").mode("01").build())
					.header(Header.builder().header("18DA10F1").mode("22").build())
					.delayAfterInit(0)
			        .protocol(Protocol.AUTO)
			        .sequence(DefaultCommandGroup.INIT)
			        .build();
			
			List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra, query, init).get();
			
			Assertions.assertThat(collection).isNotEmpty().hasSize(3);
			Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1827 181F 1864");
			Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1910 18C8 1828");
			Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 186F");
	}
	
	
	//7046, 7035, 7037, 7021, 7047, 7025, 7003, 7009, 7016, 7002, 17078, 7014, 7028, 7005, 7036, 7076, 7019, 7020, 7018
	@Test
	public void aggregateAllGroupsTest() {
			final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json","giulia_2.0_gme.json");
			final Query query = Query.builder()
					.pid(pidRegistry.findBy(7046l).getId())
					.pid(pidRegistry.findBy(7035l).getId())
					.pid(pidRegistry.findBy(7037l).getId())
			        .pid(pidRegistry.findBy(7021l).getId())
			        .pid(pidRegistry.findBy(7047l).getId())
			        .pid(pidRegistry.findBy(7025l).getId())
				    .pid(pidRegistry.findBy(7003l).getId())
			        .pid(pidRegistry.findBy(7009l).getId())
			        .pid(pidRegistry.findBy(7016l).getId())
			        .pid(pidRegistry.findBy(7002l).getId())
			        .pid(pidRegistry.findBy(7014l).getId())
			        .pid(pidRegistry.findBy(7028l).getId())
			        .pid(pidRegistry.findBy(7005l).getId())
			        .pid(pidRegistry.findBy(7036l).getId())
			        .pid(pidRegistry.findBy(7076l).getId())
			        .pid(pidRegistry.findBy(7019l).getId())
			        .pid(pidRegistry.findBy(7020l).getId())
			        .pid(pidRegistry.findBy(7018l).getId())
			        
			   
			        .build();
			
			Adjustments extra = Adjustments.builder()
					.stNxx(STNxxExtensions.builder()
							.enabled(Boolean.TRUE)
							.promoteAllGroupsEnabled(Boolean.TRUE).build())
					.batchPolicy(BatchPolicy.builder()
							.otherModesBatchSize(10)
							.enabled(Boolean.TRUE)
							.responseLengthEnabled(false)
							.build())
					.build();
			
			final Init init = Init.builder()
					.header(Header.builder().header("18DB33F1").mode("01").build())
					.header(Header.builder().header("18DA10F1").mode("22").build())
					.delayAfterInit(0)
			        .protocol(Protocol.AUTO)
			        .sequence(DefaultCommandGroup.INIT)
			        .build();
			
			List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra, query, init).get();
			collection.stream().forEach(p -> System.out.println(p));
			
			Assertions.assertThat(collection).isNotEmpty().hasSize(4);
			Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1002 1942 1937 130A 18F0 2001 1302 1003 18BA 1935");
			Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1004 1001 19BD 3A41 1956 0300");
			Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("STPX D:22 1018");
			Assertions.assertThat(collection.get(3).getQuery()).isEqualTo("STPX D:22 04FE");

	}
	
	@Test
	public void aggregationTest() {
		final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json","giulia_2.0_gme.json");
		final Query query = Query.builder()
				.pid(12l)
		        .pid(13l)
		        .pid(18l)
		        .pid(pidRegistry.findBy(7019l).getId())
				.pid(pidRegistry.findBy(7016l).getId())
				.pid(pidRegistry.findBy(7002l).getId())
		        .pid(pidRegistry.findBy(7003l).getId())
		        .pid(pidRegistry.findBy(7005l).getId())
		        .pid(pidRegistry.findBy(7007l).getId())
			    .pid(pidRegistry.findBy(7006l).getId())
		        .pid(pidRegistry.findBy(7018l).getId())
		        .pid(pidRegistry.findBy(7015l).getId())
		        .pid(pidRegistry.findBy(7014l).getId())
		        .pid(pidRegistry.findBy(7017l).getId())
		        .pid(pidRegistry.findBy(7020l).getId())
		   
		        .build();
		
		Adjustments extra = Adjustments.builder()
				.stNxx(STNxxExtensions.builder()
						.enabled(Boolean.TRUE)
						.promoteSlowGroupsEnabled(Boolean.TRUE).build())
				.batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE)
						.responseLengthEnabled(true)
						.build())
				.build();
		
		final Init init = Init.builder()
				.header(Header.builder().header("18DB33F1").mode("01").build())
				.header(Header.builder().header("18DA10F1").mode("22").build())
				.delayAfterInit(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();
		
		List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra, query, init).get();
		Assertions.assertThat(collection).isNotEmpty().hasSize(5);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1937 1924 181F 130A 1004 18BA 1935 1302 3A58, R:5");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 19BD, R:1");
		Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 3A41, R:1");

	
		extra = Adjustments.builder()
				.stNxx(STNxxExtensions.builder()
					.enabled(Boolean.TRUE)
					.promoteSlowGroupsEnabled(Boolean.FALSE).build())
				.batchPolicy(BatchPolicy.builder()
						.enabled(Boolean.TRUE)
						.responseLengthEnabled(true)
						.build())
				.build();
		
		collection = new CommandsSuplier(pidRegistry, extra, query, init).get();
		Assertions.assertThat(collection).isNotEmpty().hasSize(6);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1937 1924 181F 130A, R:3");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1004 18BA 1935 1302 3A58, R:3");
		Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 19BD, R:1");
	}
	
	@Test
	public void priorityTestForNonBatchCommands() {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json","giulia_2.0_gme.json");
		final Query query = Query.builder()
				.pid(7018l)
				.pid(7001l) 
				.pid(7005l)
		        .pid(7006l)
		        .pid(7007l)
		        .pid(7008l)
		        .pid(7010l)
		        .pid(7021l)
		        .pid(7022l)
		        .pid(7023l)
		        .pid(7025l)
		        .pid(7029l)
		        .build();
		
		final Adjustments extra = Adjustments
				.builder()
				.stNxx(STNxxExtensions.builder()
						.enabled(Boolean.TRUE)
						.promoteSlowGroupsEnabled(Boolean.TRUE).build())
				.batchPolicy(BatchPolicy.builder()
						.responseLengthEnabled(true)
						.enabled(Boolean.TRUE).build())
				.build();
		
		final Init init = Init.builder()
				.header(Header.builder().header("18DB33F1").mode("01").build())
				.header(Header.builder().header("18DA10F1").mode("22").build())
				.header(Header.builder().header("18DA18F1").mode("555").build())
				.delayAfterInit(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();
		
		final List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra ,query, init).get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(4);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F 180E 1867, R:6");
		Assertions.assertThat(collection.get(0).getPriority()).isEqualTo(0);

		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 1956, R:1");
		Assertions.assertThat(collection.get(1).getPriority()).isEqualTo(5);

		Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("STPX H:18DA18F1, D:22 051A, R:1");
		Assertions.assertThat(collection.get(2).getPriority()).isEqualTo(0);
		
		Assertions.assertThat(collection.get(3).getQuery()).isEqualTo("STPX H:18DA18F1, D:22 04FE, R:1");
		Assertions.assertThat(collection.get(3).getPriority()).isEqualTo(2);

	}
}
