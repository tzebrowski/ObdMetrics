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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;

public class WorkflowConcurrentTest {
	
	@Test
	public void concurrentStartTest() throws IOException, InterruptedException, ExecutionException {

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

		final Callable<Integer> task = () -> {
			try {
				WorkflowExecutionStatus status = workflow.start(connection, query, optional);
				Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.STARTED);
				WorkflowMonitor.waitUntilRunning(workflow);
				Assertions.assertThat(workflow.isRunning()).isTrue();

				WorkflowFinalizer.finalize(workflow);

				Assertions.assertThat(connection.recordedQueries()).contains("22 194F 1003 1935 2");

				Assertions.assertThat(collector.findATResetCommand()).isNotNull();

				Assertions.assertThat(workflow.isRunning()).isFalse();
				return 1;
			} catch (Exception e) {
				return 0;
			}
		};
		
		int numOfThreads = 20;
		final ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);
		final List<Future<Integer>> features = new ArrayList<>();
		
		for (int i=0; i<numOfThreads; i++) {
			features.add(executorService.submit(task));
		}

		Assertions.assertThat(features).hasSize(numOfThreads);
		
		int status = 0;
		for (int i=0; i<numOfThreads; i++) {
			try {
				status += features.get(i).get();
			} catch (Exception e) {}
		}

		Assertions.assertThat(status).isEqualTo(1);
	}
	
	
	
	@Test
	public void concurrentStart2Test() throws IOException, InterruptedException, ExecutionException {

		final Callable<Integer> task = () -> {
			try {
				
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
				
				WorkflowExecutionStatus status = workflow.start(connection, query, optional);
				Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.STARTED);
				WorkflowMonitor.waitUntilRunning(workflow);
				Assertions.assertThat(workflow.isRunning()).isTrue();

				WorkflowFinalizer.finalize(workflow);

				Assertions.assertThat(connection.recordedQueries()).contains("22 194F 1003 1935 2");

				Assertions.assertThat(collector.findATResetCommand()).isNotNull();

				Assertions.assertThat(workflow.isRunning()).isFalse();
				return 1;
			} catch (Exception e) {
				return 0;
			}
		};
		
		int numOfThreads = 20;
		final ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);
		final List<Future<Integer>> features = new ArrayList<>();
		
		for (int i=0; i<numOfThreads; i++) {
			features.add(executorService.submit(task));
		}

		Assertions.assertThat(features).hasSize(numOfThreads);
		
		int status = 0;
		for (int i=0; i<numOfThreads; i++) {
			try {
				status += features.get(i).get();
			} catch (Exception e) {}
		}

		Assertions.assertThat(status).isEqualTo(1);
	}
}
