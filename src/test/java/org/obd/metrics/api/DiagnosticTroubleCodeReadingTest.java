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
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.api.model.DtcAction;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.command.dtc.DtcDictionary;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;

import com.google.common.collect.Sets;

@Execution(ExecutionMode.CONCURRENT) // Runs methods in this class in parallel
public class DiagnosticTroubleCodeReadingTest {
	
	@Test
	public void dtcReadConfigurationEnabled() throws IOException, InterruptedException {
		// Specify lifecycle observer
		final SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify the metrics collector
		final DataCollector collector = new DataCollector();

		// Obtain the Workflow instance for mode 01
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector,"mode01.json", "giulia_2.0_gme.json");

		// Define PID's we want to query
		final Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("19020D", "00F0:5902CF26E4001:482BC10048D0082:00480")
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
		WorkflowFinalizer.finalizeAfter(workflow, 800);


		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		
		Assertions.assertThat(lifecycle.getDtc())
		.isNotNull()
		.contains(new DiagnosticTroubleCode("P26E4", "00", null, "Unknown DTC Description", 0, null, null, null, null, null))
		.contains(new DiagnosticTroubleCode("P2BC1", "00", null, "Unknown DTC Description", 0, null, null, null, null, null))
		.contains(new DiagnosticTroubleCode("U1008", "00", null, "Unknown DTC Description", 0, null, null, null, null, null));
		
	}
	
	
	@Test
	public void scheduleDtcRead() throws IOException, InterruptedException {
		final SimpleLifecycle lifecycle = new SimpleLifecycle();

		final DataCollector collector = new DataCollector();

		// Obtain the Workflow instance for mode 01
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector, "giulia_2.0_gme.json");

		// Define PID's we want to query
		final Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("19020D", "7F197804B0:5902CF0191131:8F068511CDD6012:870FD706870FD73:00920FD702870F4:0121148F0221145:8F0190170F01206:148F0220148F067:21150F01001C0F8:0230158F0105159:0F0235158F0115A:158F0500640F")
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
		        .debugEnabled(true)
		        .vehicleDtcReadingEnabled(Boolean.FALSE)
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
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);
		WorkflowMonitor.waitUntilRunning(workflow);

		workflow.scheduleDTCAction(Sets.newHashSet(DtcAction.READ));

		
		Assertions.assertThat(workflow.isRunning()).isTrue();
		WorkflowFinalizer.finalizeAfter(workflow, 1200);
		
	

		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		Assertions.assertThat(lifecycle).isNotNull();
		Assertions.assertThat(lifecycle.getReceivedDtc())
			.isNotNull()
			.contains(new DiagnosticTroubleCode("P0191", "13", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0685", "11", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("U1601", "87", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("U1706", "87", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("U1700", "92", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("U1702", "87", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0121", "14", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0190", "17", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0120", "14", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0220", "14", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0621", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0100", "1C", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0230", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0105", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0235", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0115", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0500", "65", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null));
		
	}
	
	
	
	@Test
	public void scheduleDtcSnsphotsRead() throws IOException, InterruptedException {
		final SimpleLifecycle lifecycle = new SimpleLifecycle();

		final DataCollector collector = new DataCollector();

		// Obtain the Workflow instance for mode 01
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector, "alfa.json");

		// Define PID's we want to query
		final Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("19020D", "7F197804B0:5902CF0191131:8F068511CDD6012:870FD706870FD73:00920FD702870F4:0121148F0221145:8F0190170F01206:148F0220148F067:21150F01001C0F8:0230158F0105159:0F0235158F0115A:158F0500640F")
				.requestResponse("19 04 011515 FF", "0310:59040115158F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 022014 FF", "0310:59040220148F1:000B100800016F2:6410090000200A3:340F60821410004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 012114 FF", "0310:59040121148F1:000B100800016F2:6410090000200A3:340F60821410004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 023015 FF", "0310:59040230158F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 010515 FF", "0310:59040105158F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 022114 FF", "0310:59040221148F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 D70287 FF", "0310:5904D702878F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 01001C FF", "0310:590401001C8F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 D70092 FF", "0310:5904D700928F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 D60187 FF", "0310:5904D601878F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 068511 FF", "0310:59040685118F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 D70687 FF", "0310:5904D706878F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 062115 FF", "0310:59040621158F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 050064 FF", "0310:59040500648F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 023515 FF", "0310:59040235158F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 011515 FF", "0310:59040115158F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 019017 FF", "0310:59040190178F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 019113 FF", "0310:59040191138F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				.requestResponse("19 04 012014 FF", "0310:59040120148F1:000B100800016F2:6410090000200A3:340F60821510004:0000181D10AB105:030B19350B18626:FD9C18120010047:83")
				
				.build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        
		        .sequence(DefaultCommandGroup.INIT).build();
			
		final Adjustments optional = Adjustments
		        .builder()
		        .debugEnabled(false)
		        .vehicleDtcReadingEnabled(Boolean.FALSE)
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
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);
		WorkflowMonitor.waitUntilRunning(workflow);

		workflow.scheduleDTCAction(Sets.newHashSet(DtcAction.READ_SNAPSHPOTS));

		
		Assertions.assertThat(workflow.isRunning()).isTrue();
		WorkflowFinalizer.finalizeAfter(workflow, 1300);


		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		
		Assertions.assertThat(lifecycle).isNotNull();
		final Set<DiagnosticTroubleCode> dtcs = lifecycle.getReceivedDtc();
		Assertions.assertThat(dtcs).isNotNull().size().isEqualTo(18);
		
		Assertions.assertThat(dtcs)
			.isNotNull()
			.contains(new DiagnosticTroubleCode("P0191", "13", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0685", "11", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("U1601", "87", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("U1706", "87", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("U1700", "92", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("U1702", "87", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0121", "14", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0190", "17", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0120", "14", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0220", "14", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0621", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0100", "1C", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0230", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0105", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0235", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0115", "15", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null))
			.contains(new DiagnosticTroubleCode("P0500", "65", null, DtcDictionary.UNKNOWN_DTC, 0, null, null, null, null, null));
		
		dtcs.forEach(dtc -> {
			Assertions.assertThat(dtc.getSnapshot()).isNotNull();
			Assertions.assertThat(dtc.getSnapshot().getExtractedDids()).isNotNull().size().isEqualTo(11);
		});
	}
	
	
	@Test
	public void dtcReadDisabled() throws IOException, InterruptedException {
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
				.requestResponse("19020D", "00F0:5902CF26E4001:482BC10048D0082:00480")
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

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		WorkflowFinalizer.finalize(workflow);


		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		Assertions.assertThat(lifecycle.getDtc()).isEmpty();
	}
}
