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
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.test.utils.DataCollector;
import org.obd.metrics.test.utils.MockAdapterConnection;
import org.obd.metrics.test.utils.SimpleLifecycle;
import org.obd.metrics.test.utils.SimpleWorkflowFactory;
import org.obd.metrics.test.utils.WorkflowFinalizer;
import org.obd.metrics.test.utils.WorkflowMonitor;

public class CANMessageHeaderManagerTest {
		
	@ParameterizedTest
	@CsvSource(value = { 
			"false=00C0:62010B02BF021:AB0262021E00AA=5.4921875=5.3359375=4.234375=4.765625",
			"true=00C0:62010B02BF021:AB0262021E00AA=5.4921875=5.3359375=4.234375=4.765625",
		}, delimiter = '=')
	public void singleModeTest(boolean batchEnabled, String adapterGivenResponse, 
			double frontLeftWheelExected,
			double frontRightWheelExected, 
			double rearRightWheelExected, 
			double rearLeftWheelExected) throws IOException, InterruptedException {
		
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify the metrics collector
		DataCollector collector = new DataCollector(false);

		// Obtain the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector,"mode01.json", "jeep_drive_control_module.json", "giulia_2.0_gme.json");

		// Define PID's we want to query
		Query query = Query.builder()
		        .pid(7025l) 
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
		        		.enabled(batchEnabled).build())
		        .build();
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		
		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 500);

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		
		Assertions.assertThat(recordedQueries.toString()).contains("ATSHDA10F1, 22F190");
		
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
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA10F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F190");
		
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
	public void metadataTest() throws IOException, InterruptedException {
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
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01 0B 0C 11 0D 05 0F 3");
	}
	
	@Test
	public void dtcReadTest() throws IOException, InterruptedException {
		
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
		        .vehicleDtcReadingEnabled(Boolean.TRUE)
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

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		
		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow,500);

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
		// DTC reading
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("19020D");
		
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
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01 0B 0C 11 0D 05 0F 3");
	}
	
	
	@Test
	public void dtcClearTest() throws IOException, InterruptedException {
		
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
		        .header(Header.builder().mode("14").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
			
		final Adjustments optional = Adjustments
		        .builder()
		        .vehicleDtcCleaningEnabled(Boolean.TRUE)
		        .vehicleDtcReadingEnabled(Boolean.TRUE)
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

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		
		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 500);

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
		// DTC reading
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("19020D");
		
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

		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA10F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("14FFFFFF");

		
		// querying for pids
		// switching CAN header to mode 01
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01 0B 0C 11 0D 05 0F 3");
	}
}
