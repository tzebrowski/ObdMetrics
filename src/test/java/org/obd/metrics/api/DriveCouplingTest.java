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
import java.util.concurrent.BlockingDeque;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.DiagnosticRequestID;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.connection.MockAdapterConnection;

public class DriveCouplingTest {
	
	@Test
	public void batchOffTest() throws IOException, InterruptedException {
		
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify the metrics collector
		DataCollector collector = new DataCollector(false);

		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector,"mode01.json", "jeep_driveline_coupling.json");

		// Define PID's we want to query
		Query query = Query.builder()
		        .pid(58l) 
		        .pid(59l) 
		        .pid(60l) 
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("22 010B", "00C0:62010B02BF021:AB0262021E00AA")
				.requestResponse("22 010A", "00C0:62010B02BF021:AB0262021E00AA")
				.requestResponse("22 0119", "00C0:62010B02BF021:AB0262021E00AA")
				.build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .dri(DiagnosticRequestID.builder().key("22").value("DA10F1").build())
				.dri(DiagnosticRequestID.builder().key("01").value("DB33F1").build())
				.dri(DiagnosticRequestID.builder().key("556").value("DA1AF1").build())
				.protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
			
		final Adjustments optional = Adjustments
		        .builder()
		        .debugEnabled(true)
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
		        .batchPolicy(
		        		BatchPolicy
		        		.builder()
		        		.responseLengthEnabled(Boolean.FALSE)
		        		.enabled(Boolean.FALSE).build())
		        .build();
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 1500);

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
		// switching CAN header to mode 556
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA1AF1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22 0119");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22 010B");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22 010A");
	}
	
	@Test
	public void batchOnfTest() throws IOException, InterruptedException {
		
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify the metrics collector
		DataCollector collector = new DataCollector(false);

		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector,"mode01.json", "jeep_driveline_coupling.json");

		// Define PID's we want to query
		Query query = Query.builder()
		        .pid(58l) 
		        .pid(59l) 
		        .pid(60l) 
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("22 0119 010A 010B", "00C0:62010B02BF021:AB0262021E00AA")
				.build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .dri(DiagnosticRequestID.builder().key("22").value("DA10F1").build())
				.dri(DiagnosticRequestID.builder().key("01").value("DB33F1").build())
				.dri(DiagnosticRequestID.builder().key("556").value("DA1AF1").build())
				.protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
			
		final Adjustments optional = Adjustments
		        .builder()
		        .debugEnabled(true)
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
		        .batchPolicy(
		        		BatchPolicy
		        		.builder()
		        		.responseLengthEnabled(Boolean.FALSE)
		        		.enabled(Boolean.TRUE).build())
		        .build();
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 1500);

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
		// switching CAN header to mode 556
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA1AF1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22 0119 010A 010B");
	}
}
