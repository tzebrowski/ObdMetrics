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
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.connection.MockAdapterConnection;

public class STNxxxDecoderTest {

	@Test
	public void giulia_gme_2_0() throws IOException, InterruptedException {

		final SimpleLifecycle simpleLifecycle = new SimpleLifecycle();
		
		//Create an instance of DataCollector that receives the OBD Metrics
		final DataCollector collector = new DataCollector(true);

		//Getting the Workflow instance for mode 22
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(simpleLifecycle, collector);
		
		//Query for specified PID's like: Engine coolant temperature
		final Query query = Query.builder()
		        .pid(13l) // Coolant
		        .pid(15l) // RPM
		        .pid(18l) // Intake temp
		        .build();

		//Create an instance of mock connection with additional commands and replies 
		final MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("STPX H:DB33F1, D:01 0C 11 0E, R:2", "0080:410C0000112D1:0E800000000000410C0000")
		        .build();
		
		// Set target frequency
		final int targetCommandFrequency = 4;

		// Enable adaptive timing
		final Adjustments optional = Adjustments
				.builder()
		        .debugEnabled(Boolean.TRUE)
				.batchPolicy(BatchPolicy.builder().enabled(true).build())
				.stNxx(STNxxExtensions.builder().enabled(true).build())
		        .vehicleCapabilitiesReadingEnabled(Boolean.FALSE)
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .checkInterval(10)
		                .commandFrequency(targetCommandFrequency)
		                .build())
		        .cachePolicy(CachePolicy.builder().resultCacheEnabled(Boolean.FALSE).build())
		        .build();

		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder()
		        		.mode("22").header("DA10F1").build())
				.header(Header.builder()
						.mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
		
		//Start background threads, that call the adapter,decode the raw data, and populates OBD metrics
		workflow.start(connection, query,init,optional);

		// Starting the workflow completion job, it will end workflow after given period of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 2800);
		
		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
	}

}
