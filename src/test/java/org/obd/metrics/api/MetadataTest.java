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

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;

public class MetadataTest {

	@Test
	public void test() throws IOException, InterruptedException {
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
		        .build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
			
		final Adjustments optional = Adjustments
		        .builder()
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

		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		Assertions.assertThat(lifecycle.getMetadata()).containsEntry("Vehicle Identification Number", "ZAREAEBN9K7617289");
		Assertions.assertThat(lifecycle.getMetadata()).containsEntry("ECU serial number", "TD4190959E01440");
		Assertions.assertThat(lifecycle.getMetadata()).containsEntry("Spare part number", "50559852");
		Assertions.assertThat(lifecycle.getMetadata()).containsEntry("FIAT drawing number", "52055320");
		Assertions.assertThat(lifecycle.getMetadata()).containsEntry("Hardware number", "MM10JAHW232");
		Assertions.assertThat(lifecycle.getMetadata()).containsEntry("Software number", "P141VA0E");
		Assertions.assertThat(lifecycle.getMetadata()).containsEntry("Software version", "0000");
		Assertions.assertThat(lifecycle.getMetadata()).containsEntry("Hardware version", "00");
		Assertions.assertThat(lifecycle.getMetadata()).containsEntry("Functioning time (EEPROM)", "49095");
		Assertions.assertThat(lifecycle.getMetadata()).containsEntry("Operating time", "49096");	
	}
}
