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
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.connection.MockAdapterConnection;
import org.obd.metrics.pid.PidDefinition;

public class WorkflowTaskTest {
	
	@Test
	public void rejectTest() throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Getting the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		// Query for specified PID's like: Engine coolant temperature
		Query query = Query.builder()
		        .pid(6015l)  // Oil temp
		        .pid(6008l)  // Coolant
		        .pid(6007l) // IAT
		        .build();
		
		PidDefinition coolant = workflow.getPidRegistry().findBy(6007l);
		// Setting the alert threshold
		coolant.getAlert().setUpperThreshold(2);
		
		// Create an instance of mock connection with additional commands and replies
		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("22 194F 1003 1935 2", "00B0:62194F2E65101:0348193548").build();

		// Enabling batch commands
		final Adjustments optional = Adjustments
		        .builder()
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

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		WorkflowExecutionStatus status = workflow.start(connection, query, optional);
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.STARTED);
		
		// Workflow is running
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		
		// Getting the Workflow instance for mode 01
		Workflow workflow2 = SimpleWorkflowFactory.getWorkflow(collector);
		status = workflow2.start(connection, query, optional);
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.REJECTED);
		
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		
		Assertions.assertThat(workflow2.isRunning()).isFalse();
		
		
		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		
		WorkflowFinalizer.finalize(workflow);

		// Ensure batch commands were sent out
		Assertions.assertThat(connection.recordedQueries())
			.contains("22 194F 1003 1935 2");

		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		// Workflow is not running
		Assertions.assertThat(workflow.isRunning()).isFalse();
	}
}
