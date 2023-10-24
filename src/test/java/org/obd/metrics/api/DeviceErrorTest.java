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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.connection.MockAdapterConnection;
import org.obd.metrics.executor.CommandExecutionStatus;

public class DeviceErrorTest {
	
	@ParameterizedTest
	@CsvSource(value = { 
			"can Error=CANERROR",
			"bus init=BUSINIT",
			"STOPPED=STOPPED", 
			"ERROR=ERROR",
			"Unable To Connect=UNABLETOCONNECT"}, delimiter = '=')
	public void errorsTest(String given, String expctedErrorMessage) throws IOException, InterruptedException {
		// Enabling batch commands
		final Adjustments optional = Adjustments
		        .builder()
		        .debugEnabled(true)
		        .vehicleDtcCleaningEnabled(false)
		        .vehicleDtcReadingEnabled(false)
		        .vehicleMetadataReadingEnabled(false)
		        .cachePolicy(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .checkInterval(5)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
		        .build();
		
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);
		lifecycle.reset();

		Query query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		MockAdapterConnection connection = MockAdapterConnection
		        .builder()
		        .requestResponse("ATRV", "12v")
		        .requestResponse("0100", "4100BE3EA813")
		        .requestResponse("0200", "4140FED00400")
		        .requestResponse("01 15 1", given)
		        .build();

		WorkflowExecutionStatus status = workflow.start(connection, query, optional);
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.STARTED);
		
		WorkflowFinalizer.finalize(workflow);
			
		Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
		Assertions.assertThat(lifecycle.getMessage())
			.describedAs(lifecycle.getMessage())
			.isEqualTo(expctedErrorMessage);
		
	}
	
	@Test
	public void timeoutTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		
		Adjustments optional = Adjustments
		        .builder()
		        .vehicleMetadataReadingEnabled(Boolean.FALSE)
		        .vehicleCapabilitiesReadingEnabled(Boolean.FALSE)
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
		
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		@SuppressWarnings("serial")
		Map<String, String> errors = new HashMap<String, String>() {
			{
				put("FCRXTIMEOUT", CommandExecutionStatus.ERR_TIMEOUT.getMessage());
			}
		};

		for (final Entry<String, String> input : errors.entrySet()) {
			lifecycle.reset();

			Query query = Query.builder()
			        .pid(22l)
			        .pid(23l)
			        .build();

			MockAdapterConnection connection = MockAdapterConnection
			        .builder()
			        .requestResponse("ATRV", "12v")
			        .requestResponse("0100", "4100BE3EA813")
			        .requestResponse("0200", "4140FED00400")
			        .requestResponse("01 15 1", input.getKey())
			        .build();

			workflow.start(connection, query,optional);

			WorkflowFinalizer.finalizeAfter(workflow,1000);

			Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
			Assertions.assertThat(lifecycle.getMessage()).isEqualTo(input.getValue());
		}
	}
	
	
	@Test
	public void lvresetTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		
		Adjustments optional = Adjustments
		        .builder()
		        .vehicleMetadataReadingEnabled(Boolean.FALSE)
		        .vehicleCapabilitiesReadingEnabled(Boolean.FALSE)
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
		
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		@SuppressWarnings("serial")
		Map<String, String> errors = new HashMap<String, String>() {
			{
				put("LVRESET", CommandExecutionStatus.ERR_LVRESET.getMessage());
			}
		};

		for (final Entry<String, String> input : errors.entrySet()) {
			lifecycle.reset();

			Query query = Query.builder()
			        .pid(22l)
			        .pid(23l)
			        .build();

			MockAdapterConnection connection = MockAdapterConnection
			        .builder()
			        .requestResponse("ATRV", "12v")
			        .requestResponse("0100", "4100BE3EA813")
			        .requestResponse("0200", "4140FED00400")
			        .requestResponse("01 15 1", input.getKey())
			        .build();

			workflow.start(connection, query,optional);

			WorkflowFinalizer.finalizeAfter(workflow,1000);

			Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
			Assertions.assertThat(lifecycle.getMessage()).isEqualTo(input.getValue());
		}
	}
}
