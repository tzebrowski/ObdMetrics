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
import java.util.List;
import java.util.concurrent.BlockingDeque;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.connection.MockAdapterConnection;
import org.obd.metrics.pid.PidDefinition;

public class WorkflowUpdateQueryTest {
	
	
	@ParameterizedTest
	@CsvSource(value = { 
			"false=00C0:62010B02BF021:AB0262021E00AA=5.4921875=5.3359375=4.234375=4.765625",
			"true=00C0:62010B02BF021:AB0262021E00AA=5.4921875=5.3359375=4.234375=4.765625",
		}, delimiter = '=')
	public void canHeaderChangeTest(boolean batchEnabled, String adapterGivenResponse, double frontLeftWheelExected,double frontRightWheelExected, 
			double rearRightWheelExected,double rearLeftWheelExected) throws IOException, InterruptedException {
		
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify the metrics collector
		DataCollector collector = new DataCollector(false);

		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector,"mode01.json", "jeep_drive_control_module.json","giulia_2.0_gme.json");

		// Define PID's we want to query
		Query query = Query.builder()
		        .pid(50l) 
		        .pid(51l) 
		        .pid(52l) 
		        .pid(53l) 
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("22 010B", adapterGivenResponse)
				.build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
				.header(Header.builder().mode("556").header("DA1AF1").build())
				.header(Header.builder().mode("555").header("DA18F1").build())
				.protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
			
		final Adjustments optional = Adjustments
		        .builder()
		        .debugEnabled(false)
		        .vehicleDtcReadingEnabled(Boolean.FALSE)
		        .vehicleMetadataReadingEnabled(Boolean.FALSE)
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
		        		.enabled(batchEnabled).build())
		        .build();
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);
		
		Thread.sleep(1000);
		
		query = Query.builder()
		        .pid(7025l) 
		        .build();
		
		workflow.updateQuery(query, init, optional);
		
		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 2000);

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		
		// CAN headers switch check.
		Assertions.assertThat(recordedQueries.toString()).contains("ATSHDB33F1, 0100");
		Assertions.assertThat(recordedQueries.toString()).contains("ATSHDA1AF1, 22 010B");
		Assertions.assertThat(recordedQueries.toString()).contains("ATSHDA18F1, "  + (batchEnabled ? "22 04FE" : "2204FE"));

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
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22 010B");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22 010B");
		
		// switching CAN header to mode 555
		boolean changeCanAferUpdate = false;
		for (int i=0; i<6; i++) {
			changeCanAferUpdate = recordedQueries.pop().equals("10 01");
			if (changeCanAferUpdate) {
				break;
			}
		}
		Assertions.assertThat(changeCanAferUpdate).isTrue().describedAs("Did not found: 10 01");
		
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA18F1");

		Assertions.assertThat(recordedQueries.pop()).isEqualTo(batchEnabled ? "22 04FE" : "2204FE");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo(batchEnabled ? "22 04FE" : "2204FE");
				
		//front left
		final PidDefinition frontLeftWheel = workflow.getPidRegistry().findBy(50L);
		Assertions.assertThat(frontLeftWheel).isNotNull();
		final List<ObdMetric> frontLeftWheelMetrics = collector.findMetricsBy(frontLeftWheel);
		Assertions.assertThat(frontLeftWheelMetrics).isNotEmpty();
		Assertions.assertThat(frontLeftWheelMetrics.get(0).getValue()).isEqualTo(frontLeftWheelExected);
		

		//front right
		final PidDefinition frontRightWheel = workflow.getPidRegistry().findBy(51L);
		Assertions.assertThat(frontRightWheel).isNotNull();
		final List<ObdMetric> frontRightWheelMetrics = collector.findMetricsBy(frontRightWheel);
		Assertions.assertThat(frontRightWheelMetrics).isNotEmpty();
		Assertions.assertThat(frontRightWheelMetrics.get(0).getValue()).isEqualTo(frontRightWheelExected);
		
		//rear left
		final PidDefinition rearLeftWheel = workflow.getPidRegistry().findBy(52L);
		Assertions.assertThat(rearLeftWheel).isNotNull();
		final List<ObdMetric> rearLeftWheelMetrics = collector.findMetricsBy(rearLeftWheel);
		Assertions.assertThat(rearLeftWheelMetrics).isNotEmpty();
		Assertions.assertThat(rearLeftWheelMetrics.get(0).getValue()).isEqualTo(rearLeftWheelExected);
		
		
		//rear right
		final PidDefinition rearRightWheel = workflow.getPidRegistry().findBy(53L);
		Assertions.assertThat(rearRightWheel).isNotNull();
		final List<ObdMetric> rearRightWheelMetrics = collector.findMetricsBy(rearRightWheel);
		Assertions.assertThat(rearRightWheelMetrics).isNotEmpty();
		Assertions.assertThat(rearRightWheelMetrics.get(0).getValue()).isEqualTo(rearRightWheelExected);
	}
	
	
	@Test
	public void updatedTest() throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Getting the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		// Query for specified PID's like: Engine coolant temperature
		Query query1 = Query.builder()
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

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		WorkflowExecutionStatus status = workflow.start(connection, Query.builder().pid(14L).build(), optional);
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.STARTED);
		
		// Workflow is running
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();

		status = workflow.updateQuery(query1, Init.DEFAULT, optional);
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.UPDATED);
		
		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 3000);

		// Ensure batch commands were sent out
		Assertions.assertThat(connection.recordedQueries())
			.contains("22 194F 1003 1935 2");

		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		// Workflow is not running
		Assertions.assertThat(workflow.isRunning()).isFalse();
	}
	
	
	@Test
	public void notRunningTest() throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Getting the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		// Query for specified PID's like: Engine coolant temperature
		Query query1 = Query.builder()
		        .pid(6015l)  // Oil temp
		        .pid(6008l)  // Coolant
		        .pid(6007l) // IAT
		        .build();
		
		PidDefinition coolant = workflow.getPidRegistry().findBy(6007l);
		// Setting the alert threshold
		coolant.getAlert().setUpperThreshold(2);
	
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

	
		// Workflow is running
		Assertions.assertThat(workflow.isRunning()).isFalse();
		WorkflowExecutionStatus status = workflow.updateQuery(query1, Init.DEFAULT, optional);
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.NOT_RUNNING);
		
		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 5000);
	}
}
