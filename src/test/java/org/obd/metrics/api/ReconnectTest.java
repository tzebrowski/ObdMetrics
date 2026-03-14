 /**
 * Copyright 2019-2026, Tomasz Żebrowski
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.obd.metrics.api.model.ErrorsPolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReconnectTest {

	@ParameterizedTest
	@CsvSource(value = { 
			"STPXH:18DA10F1,D:221004,R:1BUSINIT=BUSINIT=true",
			"STPXH:18DA10F1,D:221004,R:1BUSBUSY=BUSBUSY=false",
			"STPXH:18DA18F1,D:22051A,R:1STOPPED=STOPPED=false",
			"STPXH:18DA18F1,D:221018,R:1CANERROR=CANERROR=false",
			"STPXH:18DA10F1,BUSINIT,D:221004,R:1=BUSINIT=true",
			"STPXH:18DA10F1,BUSBUSY,D:221004,R:1=BUSBUSY=false",
			"STPXH:18DA18F1,STOPPED,D:22051A,R:1=STOPPED=false",
			"STPXH:18DA18F1,CANERROR,D:221018,R:1=CANERROR=false",
			"lvreset=LVRESET=true",
			"can Error=CANERROR=false",
			"bus init=BUSINIT=true",
			"fCRXTIMEOUt=FCRXTIMEOUT=true",
			"stOPPED=STOPPED=false", 
			"ERROR=ERROR=true",
			"NO_DATA=NO_DATA=false",
			"Unable To Connect=UNABLETOCONNECT=true"}, delimiter = '=')
	public void reconnectTest(String given, String expctedErrorMessage, boolean shallReceiveError) throws IOException, InterruptedException {
		// Enabling batch commands
		final Adjustments optional = Adjustments
		        .builder()
		        .errorsPolicy(ErrorsPolicy.builder().numberOfRetries(2).reconnectEnabled(true).build())
		        .debugEnabled(false)
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
		WorkflowMonitor.waitUntilRunning(workflow);
		
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.STARTED);
		
		WorkflowFinalizer.finalizeAfter(workflow,800);
			
		log.error("Received error: {} = {} = {}",lifecycle.isErrorOccurred(),shallReceiveError, lifecycle.getMessage() );
		Assertions.assertThat(lifecycle.isErrorOccurred()).isEqualTo(shallReceiveError);
		
		if (shallReceiveError) {
			Assertions.assertThat(lifecycle.getMessage())
				.describedAs(lifecycle.getMessage())
				.isEqualTo(expctedErrorMessage);
		}
	}
	
	
	@Test
	public void reconnectErrorTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		Query query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("0115", "4115FFff")
		        .simulateErrorInReconnect(true)
		        .simulateWriteError(true) // simulate write error
		        .build();

		workflow.start(connection, query, Init.DEFAULT, Adjustments.DEFAULT);

		WorkflowFinalizer.finalizeAfter(workflow,1000);

		Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
	}
}
