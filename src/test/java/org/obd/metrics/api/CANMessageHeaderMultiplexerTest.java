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
import java.util.List;
import java.util.concurrent.BlockingDeque;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.api.model.*;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.test.*;

public class CANMessageHeaderMultiplexerTest {
		
	@ParameterizedTest
	@CsvSource(value = { 
			"false=00C0:62010B02BF021:AB0262021E00AA=5.4921875=5.3359375=4.234375=4.765625",
			"true=00C0:62010B02BF021:AB0262021E00AA=5.4921875=5.3359375=4.234375=4.765625",
		}, delimiter = '=')
	public void singleModeTest(boolean batchEnabled, String adapterGivenResponse, 
			double frontLeftWheelExpected,
			double frontRightWheelExpected, 
			double rearRightWheelExpected, 
			double rearLeftWheelExpected) throws IOException, InterruptedException {
		
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		DataCollector collector = new DataCollector(false); // Disables baseline default constructor tracking
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector, "mode01.json", "jeep_drive_control_module.json", "giulia_2.0_gme.json");

		Query query = Query.builder()
		        .pid(7025L) 
				.pid(50L) 
		        .pid(51L) 
		        .pid(52L) 
		        .pid(53L) 
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("22 010B", adapterGivenResponse)
				.build();
		
		Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
				.header(Header.builder().mode("556").header("DA1AF1").build())
				.header(Header.builder().mode("555").header("DA18F1").build())
				.protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
			
		Adjustments optional = Adjustments.builder()
		        .debugEnabled(true)
		        .vehicleDtcReadingEnabled(false)
		        .vehicleMetadataReadingEnabled(true)
		        .vehicleCapabilitiesReadingEnabled(true)	
		        .cachePolicy(CachePolicy.builder().storeResultCacheOnDisk(false).resultCacheEnabled(false).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy.builder().enabled(false).commandFrequency(6).build())
		        .producerPolicy(ProducerPolicy.builder().priorityQueueEnabled(true).build())
		        .batchPolicy(BatchPolicy.builder().calculateResponseFrames(false).enabled(batchEnabled).build())
		        .build();
		
		workflow.start(connection, query, init, optional);
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		
		WorkflowFinalizer.finalizeAfter(workflow, 500);

		BlockingDeque<String> recordedQueries = connection.recordedQueries();
		
		Assertions.assertThat(recordedQueries.toString()).contains("ATSHDA10F1, 22F190");
		Assertions.assertThat(recordedQueries.toString()).contains("ATSHDB33F1, 0100");
		Assertions.assertThat(recordedQueries.toString()).contains("ATSHDA1AF1, 22 010B");
		Assertions.assertThat(recordedQueries.toString()).contains("ATSHDA18F1, "  + (batchEnabled ? "22 04FE" : "2204FE"));

		AssertHelper.assertInitializationCommands(recordedQueries);
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA10F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("22F190");
		
		// Assert Wheel Metrics Values Decoding
		assertMetricValue(workflow, collector, 50L, frontLeftWheelExpected);
		assertMetricValue(workflow, collector, 51L, frontRightWheelExpected);
		assertMetricValue(workflow, collector, 52L, rearLeftWheelExpected);
		assertMetricValue(workflow, collector, 53L, rearRightWheelExpected);
	}
	
	@Test
	public void metadataTest() throws IOException, InterruptedException {
		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), collector, "mode01.json", "giulia_2.0_gme.json");

		Query query = createBaseQueryBuilder().build();
		MockAdapterConnection connection = createStandardMockConnection();
		Init init = createBaseInitBuilder().build();
		Adjustments optional = createBaseAdjustmentsBuilder().build();
		
		workflow.start(connection, query, init, optional);
		WorkflowFinalizer.finalizeAfter(workflow, 800);

		BlockingDeque<String> recordedQueries = connection.recordedQueries();

		AssertHelper.assertInitializationCommands(recordedQueries);
		assertBaseVehicleDiscovery(recordedQueries);
		assertSupportedModesDiscovery(recordedQueries);
		
		// Final tracking mode assertion
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01 0B 0C 11 0D 05 0F 2");
	}
	
	@Test
	public void dtcReadTest() throws IOException, InterruptedException {
		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), collector, "mode01.json", "giulia_2.0_gme.json");

		Query query = createBaseQueryBuilder().build();
		MockAdapterConnection connection = createStandardMockConnection();
		Init init = createBaseInitBuilder().build();
		Adjustments optional = createBaseAdjustmentsBuilder().vehicleDtcReadingEnabled(true).build();
		
		workflow.start(connection, query, init, optional);
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		
		WorkflowFinalizer.finalizeAfter(workflow, 500);

		BlockingDeque<String> recordedQueries = connection.recordedQueries();
		
		AssertHelper.assertInitializationCommands(recordedQueries);
		assertBaseVehicleDiscovery(recordedQueries);
		
		// DTC reading specific execution boundary check
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("19020D");
		
		assertSupportedModesDiscovery(recordedQueries);
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01 0B 0C 11 0D 05 0F 2");
	}
	
	@Test
	public void dtcClearTest() throws IOException, InterruptedException {
		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), collector, "mode01.json", "giulia_2.0_gme.json");

		Query query = createBaseQueryBuilder().build();
		MockAdapterConnection connection = createStandardMockConnection();
		
		Init init = createBaseInitBuilder()
		        .header(Header.builder().mode("14").header("DA10F1").build()) // Clear loop instruction registration
		        .build();
			
		Adjustments optional = createBaseAdjustmentsBuilder()
		        .vehicleDtcCleaningEnabled(true)
		        .vehicleDtcReadingEnabled(true)
		        .build();
		
		workflow.start(connection, query, init, optional);
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		
		WorkflowFinalizer.finalizeAfter(workflow, 500);

		BlockingDeque<String> recordedQueries = connection.recordedQueries();
		
		AssertHelper.assertInitializationCommands(recordedQueries);
		assertBaseVehicleDiscovery(recordedQueries);
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("19020D");
		
		assertSupportedModesDiscovery(recordedQueries);

		// DTC Clear Command Verification
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDA10F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("14FFFFFF");
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01 0B 0C 11 0D 05 0F 2");
	}

	// --- Shared Helper Blueprints to Eliminate Duplication ---

	private Query.QueryBuilder createBaseQueryBuilder() {
		return Query.builder()
		        .pid(6L)   // Engine coolant temperature
		        .pid(12L)  // Intake manifold absolute pressure
		        .pid(13L)  // Engine RPM
		        .pid(16L)  // Intake air temperature
		        .pid(18L)  // Throttle position
		        .pid(14L); // Vehicle speed
	}

	private MockAdapterConnection createStandardMockConnection() {
		return MockAdapterConnection.builder()
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
	}

	private Init.InitBuilder createBaseInitBuilder() {
		return Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT);
	}

	private Adjustments.AdjustmentsBuilder createBaseAdjustmentsBuilder() {
		return Adjustments.builder()
		        .vehicleDtcReadingEnabled(false)
		        .vehicleMetadataReadingEnabled(true)
		        .vehicleCapabilitiesReadingEnabled(true)	
		        .cachePolicy(CachePolicy.builder().storeResultCacheOnDisk(false).resultCacheEnabled(false).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy.builder().enabled(false).commandFrequency(6).build())
		        .producerPolicy(ProducerPolicy.builder().priorityQueueEnabled(true).build())
		        .batchPolicy(BatchPolicy.builder().enabled(true).build());
	}

	private void assertBaseVehicleDiscovery(BlockingDeque<String> recordedQueries) {
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
	}

	private void assertSupportedModesDiscovery(BlockingDeque<String> recordedQueries) {
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSHDB33F1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0100");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0120");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0140");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0160");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("0180");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01A0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("01C0");
	}

	private void assertMetricValue(Workflow workflow, DataCollector collector, long pidId, double expectedValue) {
		PidDefinition pid = workflow.getPidRegistry().findBy(pidId);
		Assertions.assertThat(pid).isNotNull();
		List<ObdMetric> metrics = collector.findMetricsBy(pid);
		Assertions.assertThat(metrics).isNotEmpty();
		Assertions.assertThat(metrics.get(0).getValue()).isEqualTo(expectedValue);
	}
}