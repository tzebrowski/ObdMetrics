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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import org.obd.metrics.command.routine.RoutineExecutionStatus;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;

public class RoutinesTest {
	
	@ParameterizedTest
	@CsvSource(value = { 
			"22=DA10F1=2F50920300FF=10000=NODATA=NO_DATA",
			"22=DA10F1=2F50920300FF=10000=7F=ERROR",
			"22=DA10F1=2F50920300FF=10000=6F5092=SUCCESS",
			"INSTRUMENT_PANEL=DA60F1=2F55720308=10002=NODATA=NO_DATA",
			"INSTRUMENT_PANEL=DA60F1=2F55720308=10002=6F557203=SUCCESS",
			"INSTRUMENT_PANEL=DA60F1=2F55720308=10002=7F2F13=ERROR"
		}, delimiter = '=')
	public void parameterizedTest(String canRequestIDKey,String canRequestIDValue, String routine, long routineID, 
			String givenECUResponse,RoutineExecutionStatus routineStatus ) 
			throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		SimpleLifecycle lifecycle = new SimpleLifecycle();
		
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector, "giulia_2.0_gme.json");

		// Query for specified PID's like: Engine coolant temperature
		Query query = Query.builder()
		        .pid(6015l)  // Oil temp
		        .pid(6008l)  // Coolant
		        .pid(6007l) // IAT
		        .build();
		// Create an instance of mock connection with additional commands and replies
		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("22 194F 1003 1935 2", "00B0:62194F2E65101:0348193548")
		        .requestResponse(routine, givenECUResponse).build();

		final Adjustments optional = getAdjustements();

		WorkflowExecutionStatus status = workflow.start(connection, query, optional);
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.STARTED);
		
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		 
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode(canRequestIDKey).header(canRequestIDValue).build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
		
		status = workflow.executeRoutine(routineID, init);
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.ROUTINE_QUEUED);
		
		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalize(workflow);

		final String expectedQueries = "ATD, ATZ, ATL0, ATH0, ATE0, ATPP 2CSV 01, ATPP 2C ON, ATPP 2DSV 01, ATPP 2D ON, ATAT2, ATSP0, ATSH" 
		+ canRequestIDValue + ", 10 03, 3E00, " + routine;
		
		for (final String q : expectedQueries.split(",")) {
			Assertions.assertThat(connection.recordedQueries().pop()).isEqualTo(q.trim());
		}
		
		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		// Workflow is not running
		Assertions.assertThat(workflow.isRunning()).isFalse();
		
		Assertions.assertThat(lifecycle.getRoutineExecutionStatus()).isEqualTo(routineStatus);
	}
	
	@Test
	public void noIDsProvidedTest() throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		SimpleLifecycle lifecycle = new SimpleLifecycle();
		
		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector, "giulia_2.0_gme.json");

		// Query for specified PID's like: Engine coolant temperature
		Query query = Query.builder()
		        .pid(6015l)  // Oil temp
		        .pid(6008l)  // Coolant
		        .pid(6007l) // IAT
		        .build();
		// Create an instance of mock connection with additional commands and replies
		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("22 194F 1003 1935 2", "00B0:62194F2E65101:0348193548")
		        .requestResponse("2F509203FF", "OK").build();

		final Adjustments optional = getAdjustements();

		WorkflowExecutionStatus status = workflow.start(connection, query, optional);
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.STARTED);
		
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
	
		 
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
				.header(Header.builder().mode("556").header("DA1AF1").build())
				.header(Header.builder().mode("555").header("DA18F1").build())
				.protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
		
		status = workflow.executeRoutine(123456L, init);
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.REJECTED);
		
		WorkflowFinalizer.finalize(workflow);

		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		// Workflow is not running
		Assertions.assertThat(workflow.isRunning()).isFalse();
	}


	private Adjustments getAdjustements() {
		// Enabling batch commands
		final Adjustments optional = Adjustments
		        .builder()
		        .debugEnabled(true)
		        .vehicleDtcCleaningEnabled(false)
		        .vehicleDtcReadingEnabled(false)
		        .vehicleMetadataReadingEnabled(false)
		        .vehicleCapabilitiesReadingEnabled(false)
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
		return optional;
	}
}
