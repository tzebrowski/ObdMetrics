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

import java.io.IOException;
import java.util.concurrent.BlockingDeque;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.connection.MockAdapterConnection;

public class CanModeOverrideTest {
	
	@Test
	public void stnOffTest() throws IOException, InterruptedException {
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify the metrics collector
		DataCollector collector = new DataCollector();

		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector,"mode01.json", "giulia_2.0_gme.json");

		// Define PID's we want to query
		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(7025l) 
		        .pid(7029l) 
		        
		        .pid(7005l) 
		        .pid(7006l) 
		        
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("22F191", "00E0:62F1913532301:353533323020202:20")
				.requestResponse("22F192", "00E0:62F1924D4D311:304A41485732332:32")
				.requestResponse("22F187", "00E0:62F1873530351:353938353220202:20")
				.requestResponse("22F190", "0140:62F1905A41521:454145424E394B2:37363137323839")
				.requestResponse("22F18C", "0120:62F18C5444341:313930393539452:3031343430")
				.requestResponse("22F194", "00E0:62F1945031341:315641304520202:20")
				.requestResponse("221008", "6210080000BFC8")
				.requestResponse("222008", "6220080000BFC7")
				.requestResponse("22F195", "62F1950000")
				.requestResponse("22F193", "62F19300")
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("0105", "410522")
		        .requestResponse("010C", "410c541B")
		        .requestResponse("010B", "410b35")
		        .requestResponse("2204FE", "6204FE4E")
		        .requestResponse("22051A", "62051A11")

		        .requestResponse("221937", "621937011C")
		        .requestResponse("222181F", "62181F0119")

		        .build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
				//overrides CAN mode
				.header(Header.builder().mode("555").header("DA18F1").build()) 
		        .protocol(Protocol.CAN_29)
		        
		        .sequence(DefaultCommandGroup.INIT).build();
			
		final Adjustments optional = Adjustments
		        .builder()
		        .vehicleDtcReadingEnabled(Boolean.FALSE)
		        .vehicleMetadataReadingEnabled(Boolean.TRUE)
		        .vehicleCapabilitiesReadingEnabled(Boolean.TRUE)	
		        .cachePolicy(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
		        .build();
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow,800);

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();

		// initialization
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATD");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2CSV 01");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2C ON");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2DSV 01");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2D ON");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATAT2");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP7");
		
		// getting vehicle properties
		// switching CAN header to mode22 
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA10F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F190");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F18C");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F194");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F191");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F192");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F187");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F196");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F195");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F193");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F1A5");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("222008");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("221008");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0902");
		
		// getting supported modes
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0100");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0120");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0140");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0160");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0180");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01A0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01C0");
		
		// querying for pids
		
		// switching can header to mode 22
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA10F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22 1937 181F 2");
		
		
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01 0B 0C 11 0D 2");
		
		// switching CAN header to virtual mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA18F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22 051A 1");
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01 05 0F 1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA18F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22 04FE 1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA10F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22 1937 181F 2");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01 0B 0C 11 0D 2");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA18F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22 051A 1");
		
	}
	
	
	@Test
	public void stnOnTest() throws IOException, InterruptedException {
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify the metrics collector
		DataCollector collector = new DataCollector();

		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector,"mode01.json", "giulia_2.0_gme.json");

		// Define PID's we want to query
		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .pid(7025l) 
		        .pid(7029l) 
		        
		        .pid(7005l) 
		        .pid(7006l) 
		        
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("22F191", "00E0:62F1913532301:353533323020202:20")
				.requestResponse("22F192", "00E0:62F1924D4D311:304A41485732332:32")
				.requestResponse("22F187", "00E0:62F1873530351:353938353220202:20")
				.requestResponse("22F190", "0140:62F1905A41521:454145424E394B2:37363137323839")
				.requestResponse("22F18C", "0120:62F18C5444341:313930393539452:3031343430")
				.requestResponse("22F194", "00E0:62F1945031341:315641304520202:20")
				.requestResponse("221008", "6210080000BFC8")
				.requestResponse("222008", "6220080000BFC7")
				.requestResponse("22F195", "62F1950000")
				.requestResponse("22F193", "62F19300")
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("0105", "410522")
		        .requestResponse("010C", "410c541B")
		        .requestResponse("010B", "410b35")
		        .requestResponse("2204FE", "6204FE4E")
		        .requestResponse("22051A", "62051A11")

		        .requestResponse("221937", "621937011C")
		        .requestResponse("222181F", "62181F0119")

		        .build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
				//overrides CAN mode
				.header(Header.builder().mode("555").header("DA18F1").build())
		        .protocol(Protocol.CAN_29)
		        
		        .sequence(DefaultCommandGroup.INIT).build();
			
		final Adjustments optional = Adjustments
		        .builder()
		        .vehicleDtcReadingEnabled(Boolean.FALSE)
		        .vehicleMetadataReadingEnabled(Boolean.TRUE)
		        .vehicleCapabilitiesReadingEnabled(Boolean.TRUE)	
		        .stNxx(STNxxExtensions.builder().enabled(Boolean.TRUE).build())
		        .cachePolicy(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
		        .build();
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow,800);

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();

		// initialization
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATD");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2CSV 01");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2C ON");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2DSV 01");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATPP 2D ON");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATAT2");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP7");
		
		// getting vehicle properties
		// switching CAN header to mode22 
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA10F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F190");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F18C");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F194");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F191");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F192");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F187");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F196");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F195");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F193");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F1A5");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("222008");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("221008");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0902");
		
		// getting supported modes
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0100");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0120");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0140");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0160");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0180");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01A0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01C0");
		
		// querying for pids
		
		// switching can header to mode 22
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STPX H:DA10F1, D:22 1937 181F, R:2");
		
		
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STPX H:DB33F1, D:01 0B 0C 11 0D, R:2");
		
		// switching CAN header to virtual mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STPX H:DA18F1, D:22 051A, R:1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STPX H:DB33F1, D:01 05 0F, R:1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STPX H:DA18F1, D:22 04FE, R:1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STPX H:DA10F1, D:22 1937 181F, R:2");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STPX H:DB33F1, D:01 0B 0C 11 0D, R:2");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STPX H:DA18F1, D:22 051A, R:1");
	}
}
