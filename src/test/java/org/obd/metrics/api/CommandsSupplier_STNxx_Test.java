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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.PIDsRegistryFactory;
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

// [22, 7002, 13, 14, 15, 7003, 7006, 6, 7005, 18, 7018, 7007, 7015, 7014, 7017, 7016, 7019, 7020]
// [[priority=0, query=STPX H:18DA10F1, D:22 181F 1937 130A 1924, R:3], [priority=2, query=STPX H:18DA10F1, D:22 1935 1302 3A58 18BA 1004, R:3], [priority=3, query=STPX H:18DA10F1, D:22 19BD, R:1], [priority=4, query=STPX H:18DA10F1, D:22 3A41, R:1], [priority=6, query=STPX H:18DA10F1, D:22 3813, R:1], [priority=0, query=STPX H:18DB33F1, D:01 15 0C 0D 11, R:2], [priority=1, query=STPX H:18DB33F1, D:01 0E, R:1], [priority=2, query=STPX H:18DB33F1, D:01 05, R:1]]


public class CommandsSupplier_STNxx_Test {
	
	@Test
	public void limitMode1QueryTest() {
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
		        .pid(24l) 
		        .pid(25l) 
		        .build();
		
		final Adjustments extra = Adjustments
				.builder()
				.stNxx(STNxxExtensions.builder()
						.enabled(Boolean.TRUE)
						.promoteSlowGroupsEnabled(Boolean.FALSE).build())
				  .batchPolicy(BatchPolicy.builder()
						  .responseLengthEnabled(true)
						  .enabled(Boolean.TRUE).build())
				  .build();

		final List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra ,query,Init.DEFAULT).get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(4);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX D:01 15 0B 0C 11 0D 16, R:3");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX D:01 17, R:1");
		Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("STPX D:01 0E, R:1");
	}

	@Test
	public void pids11Test() {
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
		        .pid(7024l)
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
				.delayAfterInit(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();
		
		final List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra ,query, init).get();

		Assertions.assertThat(collection).isNotEmpty().hasSize(1);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F 1956 180E 1867 1802, R:6");
	}
	
	
	@Test
	public void maxDefaultsBatchSizeTest() {
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
				.delayAfterInit(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();
		
		final List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra ,query, init).get();

		Assertions.assertThat(collection).isNotEmpty().hasSize(1);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F 1956 180E 1867, R:6");
	}
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"0=1='STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F 1956 180E 1867, R:6'", 
			"1=10='STPX H:18DA10F1, D:22 130A, R:1'",
			"2=5='STPX H:18DA10F1, D:22 130A 195A, R:2'",
			"3=4='STPX H:18DA10F1, D:22 130A 195A 1937, R:2'",
			"4=3='STPX H:18DA10F1, D:22 130A 195A 1937 181F, R:3'",
			"5=2='STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924, R:3'",
			"6=2='STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000, R:4'",
			"7=2='STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F, R:5'",
			"8=2='STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F 1956, R:5'",
			"9=2='STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F 1956 180E, R:6'",
			"10=1='STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F 1956 180E 1867, R:6'", 
			"20=1='STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F 1956 180E 1867, R:6'",
			"30=1='STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F 1956 180E 1867, R:6'",
	},delimiter =  '=')
	public void customMode22BatchSizeTest(String givenBatchSize,String expectedNummberOfQueries, String expectedFirstQuery) {
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
		        .build();
		
		final Adjustments extra = Adjustments
				.builder()
				.stNxx(STNxxExtensions.builder()
					.enabled(Boolean.TRUE)
					.promoteSlowGroupsEnabled(Boolean.TRUE).build())
				.batchPolicy(BatchPolicy.builder()
					.responseLengthEnabled(true)
					.mode22BatchSize(Integer.parseInt(givenBatchSize))
					.enabled(Boolean.TRUE).build())
				.build();
		
		final Init init = Init.builder()
				.header(Header.builder().header("18DB33F1").mode("01").build())
				.header(Header.builder().header("18DA10F1").mode("22").build())
				.delayAfterInit(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();
		
		final List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra ,query, init).get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(Integer.parseInt(expectedNummberOfQueries));
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo(expectedFirstQuery.replace("'",""));
	}
	
	@Test
	public void noHeadersIncludedQueryTest() {
		final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json","giulia_2.0_gme.json");
		final Query query = Query.builder()
				.pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(18l) // Throttle position
		        
				.pid(7018l)
				.pid(7001l) 
				.pid(7005l)
		        .pid(7006l)
		        .pid(7007l)
		        .pid(7008l)
		        .pid(7010l)
		        .build();
		
		final Adjustments extra = Adjustments.builder()
				.stNxx(STNxxExtensions.builder()
						.enabled(Boolean.TRUE)
						.promoteSlowGroupsEnabled(Boolean.TRUE).build())
				.batchPolicy(BatchPolicy.builder()
						.responseLengthEnabled(true)
						.enabled(Boolean.TRUE).build())
				.build();
		
		final List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra, query, Init.DEFAULT).get();
	
		Assertions.assertThat(collection).isNotEmpty().hasSize(2);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX D:22 130A 195A 1937 181F 1924 1000 182F, R:5");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX D:01 0B 0C 11, R:2");
	}
	

	@Test
	public void headersIncludedQueryTest() {
		final PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json","giulia_2.0_gme.json");
		final Query query = Query.builder()
				.pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(18l) // Throttle position
		        
		        .pid(7018l)
				.pid(7001l) 
				.pid(7005l)
		        .pid(7006l)
		        .pid(7007l)
		        .pid(7008l)
		        .pid(7010l)
		        .build();
		
		final Adjustments extra = Adjustments.builder()
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
				.delayAfterInit(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();

		
		final List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra, query, init).get();
	
		Assertions.assertThat(collection).isNotEmpty().hasSize(2);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F, R:5");
		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX H:18DB33F1, D:01 0B 0C 11, R:2");
	}
	
	@Test
	public void promoteSlowGoupTest() {
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
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(3);
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo("STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F 1956 180E 1867, R:6");
		Assertions.assertThat(collection.get(0).getPriority()).isEqualTo(0);

		Assertions.assertThat(collection.get(1).getQuery()).isEqualTo("STPX H:18DA18F1, D:22 051A, R:1");
		Assertions.assertThat(collection.get(1).getPriority()).isEqualTo(0);

		Assertions.assertThat(collection.get(2).getQuery()).isEqualTo("STPX H:18DA18F1, D:22 04FE, R:1");
		Assertions.assertThat(collection.get(2).getPriority()).isEqualTo(2);

	}
	
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"0=1='STPX H:18DB33F1, D:01 04 06 0B 0C 07 05, R:3'", 
			"1=6='STPX H:18DB33F1, D:01 04, R:1'",
			"2=3='STPX H:18DB33F1, D:01 04 06, R:1'",
			"3=2='STPX H:18DB33F1, D:01 04 06 0B, R:2'",
			"4=2='STPX H:18DB33F1, D:01 04 06 0B 0C, R:2'",
			"5=2='STPX H:18DB33F1, D:01 04 06 0B 0C 07, R:2'",
			"6=1='STPX H:18DB33F1, D:01 04 06 0B 0C 07 05, R:3'",
	},delimiter =  '=')
	public void customMode01BatchSizeTest(String givenBatchSize,String expectedNummberOfQueries, String expectedFirstQuery) {
		PidDefinitionRegistry pidRegistry = PIDsRegistryFactory.get("mode01.json","giulia_2.0_gme.json");
		final Query query = Query.builder()
				.pid(5l) 
				.pid(6l)
		        .pid(7l)
		        .pid(8l)
		        .pid(12l)
		        .pid(13l)
		        .build();
		
		final Adjustments extra = Adjustments
				.builder()
				.stNxx(STNxxExtensions.builder()
					.enabled(Boolean.TRUE)
					.promoteSlowGroupsEnabled(Boolean.TRUE).build())
				.batchPolicy(BatchPolicy.builder()
					.responseLengthEnabled(true)
					.mode01BatchSize(Integer.parseInt(givenBatchSize))
					.enabled(Boolean.TRUE).build())
				.build();
		
		final Init init = Init.builder()
				.header(Header.builder().header("18DB33F1").mode("01").build())
				.header(Header.builder().header("18DA10F1").mode("22").build())
				.delayAfterInit(0)
		        .protocol(Protocol.AUTO)
		        .sequence(DefaultCommandGroup.INIT)
		        .build();
		
		final List<ObdCommand> collection = new CommandsSuplier(pidRegistry, extra ,query, init).get();
		
		Assertions.assertThat(collection).isNotEmpty().hasSize(Integer.parseInt(expectedNummberOfQueries));
		Assertions.assertThat(collection.get(0).getQuery()).isEqualTo(expectedFirstQuery.replace("'",""));
	}
}
