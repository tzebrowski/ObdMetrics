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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.DiagnosticTroubleCode;
import org.obd.metrics.api.model.DtcAction;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.Lifecycle;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.dtc.DiagnosticTroubleCodeClearStatus;
import org.obd.metrics.command.dtc.DtcDictionary;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.pid.Urls;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;
import org.obd.metrics.translation.TranslationProvider;

import com.google.common.collect.Sets;

import lombok.NonNull;

public class DiagnosticTroubleCodeReadingTest {

	@SuppressWarnings("unchecked")
	static <T extends ReplyObserver<?>> Workflow getWorkflow(Lifecycle lifecycle, T dataCollector, 
			String languageCode, String... pidFiles) {
		
		Pids.PidsBuilder pids = Pids.builder();
		for (final String pidFile : pidFiles) {
			pids = pids.resource(Urls.resourceToUrl(pidFile));
		}
		
		return Workflow.instance()
				.translationProvider(TranslationProvider.instance(languageCode))
				.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().scriptEngine("JavaScript").build())
				.lifecycle(lifecycle)
				.pids(pids.build())
				.observer((@NonNull ReplyObserver<Reply<?>>) dataCollector)
				.initialize();
	}

	@Test
	public void dtcReadConfigurationEnabled() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		DataCollector collector = new DataCollector();
		Workflow workflow = getWorkflow(lifecycle, collector, "en", "mode01.json", "giulia_2.0_gme.json");

		Query query = Query.builder().pid(6L).pid(12L).pid(13L).pid(16L).pid(18L).pid(14L).build();

		MockAdapterConnection connection = createBaseConnection()
				.requestResponse("19020D", "00F0:5902CF26E4001:482BC10048D0082:00480")
				.build();
		
		workflow.start(connection, query, createDefaultInit(200), createAdjustments(true, true, false));

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		WorkflowFinalizer.finalizeAfter(workflow, 800);

		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		Assertions.assertThat(lifecycle.getDtc())
				.isNotNull()
				.contains(createDtc("P26E4", "00", "Unknown DTC Description"))
				.contains(createDtc("P2BC1", "00", "Unknown DTC Description"))
				.contains(createDtc("U1008", "00", "Unknown DTC Description"));
	}
	
	@ParameterizedTest
	@CsvSource({
		"en, Test Failed, Test Failed This Operation Cycle",
		"pl, Test zakończony niepowodzeniem, Test nieudany w bieżącym cyklu pracy"
	})
	public void scheduleDtcRead(String languageCode, String expectedStatus1, String expectedStatus2) throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		DataCollector collector = new DataCollector();
		Workflow workflow = getWorkflow(lifecycle, collector, languageCode, "giulia_2.0_gme.json");

		Query query = Query.builder().pid(6L).build();

		MockAdapterConnection connection = createBaseConnection()
				.requestResponse("19020D", "7F197804B0:5902CF0191131:8F068511CDD6012:870FD706870FD73:00920FD702870F4:0121148F0221145:8F0190170F01206:148F0220148F067:21150F01001C0F8:0230158F0105159:0F0235158F0115A:158F0500640F")
				.build();
		
		workflow.start(connection, query, createDefaultInit(), createAdjustments(false, false, false));
		WorkflowMonitor.waitUntilRunning(workflow);

		workflow.scheduleDTCAction(Sets.newHashSet(DtcAction.READ));

		Assertions.assertThat(workflow.isRunning()).isTrue();
		WorkflowFinalizer.finalizeAfter(workflow, 1200);
		
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		final List<DiagnosticTroubleCode> dtcList = new ArrayList<>(lifecycle.getReceivedDtc());
		Assertions.assertThat(dtcList).isNotEmpty();
		
		Assertions.assertThat(dtcList)
				.contains(createDtc("P0191", "13", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0685", "11", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("U1601", "87", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("U1706", "87", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("U1700", "92", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("U1702", "87", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0121", "14", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0190", "17", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0120", "14", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0220", "14", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0621", "15", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0100", "1C", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0230", "15", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0105", "15", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0235", "15", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0115", "15", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0500", "65", DtcDictionary.UNKNOWN_DTC));
				
		Assertions.assertThat(dtcList.get(0).getActiveStatuses()).contains(expectedStatus1, expectedStatus2);
	}

	@Test
	public void scheduleDtcReadMultiModule() throws IOException, InterruptedException {
		// Regression test for the bug where DiagnosticTroubleCodeHandler used a
		// capacity-1 queue, silently dropping every module's DTC read but the first, and for the
		// follow-up bug where DiagnosticTroubleCode's equals()/hashCode() ignored "module", so two
		// modules reporting byte-identical codes collapsed to a single Set entry and lost their
		// per-module tag. Both modules are mocked with the same response (MockAdapterConnection
		// replies by exact request text, so distinct per-module payloads aren't possible here), so
		// every code is expected to survive once per module.
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		DataCollector collector = new DataCollector();
		Workflow workflow = getWorkflow(lifecycle, collector, "en", "giulia_2.0_gme.json");

		Query query = Query.builder().pid(6L).build();

		MockAdapterConnection connection = createBaseConnection()
				.requestResponse("19020D", "7F197804B0:5902CF0191131:8F068511CDD6012:870FD706870FD73:00920FD702870F4:0121148F0221145:8F0190170F01206:148F0220148F067:21150F01001C0F8:0230158F0105159:0F0235158F0115A:158F0500640F")
				.build();

		workflow.start(connection, query, createDefaultInit(), createAdjustments(false, false, false));
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();

		final List<Header> modules = Arrays.asList(
				Header.builder().mode("Engine").header("7E0").build(),
				Header.builder().mode("ABS").header("760").build());

		workflow.scheduleDTCAction(Collections.singleton(DtcAction.READ), modules);
		WorkflowFinalizer.finalizeAfter(workflow, 1200);

		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		final List<DiagnosticTroubleCode> dtcList = new ArrayList<>(lifecycle.getReceivedDtc());
		Assertions.assertThat(dtcList).isNotEmpty();

		Assertions.assertThat(dtcList)
				.extracting(DiagnosticTroubleCode::getModule)
				.allMatch(module -> "Engine".equals(module) || "ABS".equals(module));

		Assertions.assertThat(dtcList)
				.extracting(DiagnosticTroubleCode::getStandardCode)
				.contains("P0191", "U1601");

		// Both modules report the identical set of codes, and each module's copy of every code
		// must survive as its own entry rather than collapsing into the other module's.
		Assertions.assertThat(dtcList)
				.filteredOn(dtc -> "Engine".equals(dtc.getModule()))
				.extracting(DiagnosticTroubleCode::getStandardCode)
				.containsExactlyInAnyOrder(
						"P0191", "P0685", "U1601", "U1706", "U1700", "U1702", "P0121", "P0221", "P0190",
						"P0120", "P0220", "P0621", "P0100", "P0230", "P0105", "P0235", "P0115", "P0500");

		Assertions.assertThat(dtcList)
				.filteredOn(dtc -> "ABS".equals(dtc.getModule()))
				.extracting(DiagnosticTroubleCode::getStandardCode)
				.containsExactlyInAnyOrder(
						"P0191", "P0685", "U1601", "U1706", "U1700", "U1702", "P0121", "P0221", "P0190",
						"P0120", "P0220", "P0621", "P0100", "P0230", "P0105", "P0235", "P0115", "P0500");

		Assertions.assertThat(connection.recordedQueries().toString())
				.contains("ATSH7E0")
				.contains("ATSH760");
	}

	@Test
	public void scheduleDtcReadMultiModuleWithSharedReadQuery() throws IOException, InterruptedException {
		// giulia_2.0_gme.json and dtc_read_shared_query.json both register a DTC read PID for "19020D". The decoder
		// then decodes the reply once per registered PID, and used the registry's copies - tagged
		// with the default "ecu" module - so every code lost the module it was read from. The read
		// itself must also go out once per module, not once per registered PID.
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		DataCollector collector = new DataCollector();
		Workflow workflow = getWorkflow(lifecycle, collector, "en", "giulia_2.0_gme.json", "dtc_read_shared_query.json");

		Query query = Query.builder().pid(6L).build();

		MockAdapterConnection connection = createBaseConnection()
				.requestResponse("19020D", "7F197804B0:5902CF0191131:8F068511CDD6012:870FD706870FD73:00920FD702870F4:0121148F0221145:8F0190170F01206:148F0220148F067:21150F01001C0F8:0230158F0105159:0F0235158F0115A:158F0500640F")
				.build();

		workflow.start(connection, query, createDefaultInit(), createAdjustments(false, false, false));
		awaitRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();

		final List<Header> modules = Arrays.asList(
				Header.builder().mode("Engine").header("7E0").build(),
				Header.builder().mode("ABS").header("760").build());

		workflow.scheduleDTCAction(Collections.singleton(DtcAction.READ), modules);
		WorkflowFinalizer.finalizeAfter(workflow, 1200);

		final List<DiagnosticTroubleCode> dtcList = new ArrayList<>(lifecycle.getReceivedDtc());
		Assertions.assertThat(dtcList).isNotEmpty();
		Assertions.assertThat(dtcList)
				.extracting(DiagnosticTroubleCode::getModule)
				.containsOnly("Engine", "ABS");
		Assertions.assertThat(dtcList)
				.filteredOn(dtc -> "Engine".equals(dtc.getModule()))
				.extracting(DiagnosticTroubleCode::getStandardCode)
				.contains("P0191", "U1601");

		final List<String> recorded = new ArrayList<>(connection.recordedQueries());
		final int engine = recorded.indexOf("ATSH7E0");
		final int abs = recorded.indexOf("ATSH760");
		Assertions.assertThat(engine).isNotNegative();
		Assertions.assertThat(abs).isGreaterThan(engine);
		Assertions.assertThat(recorded.subList(engine + 1, abs)).containsExactly("19020D");
	}

	@Test
	public void scheduleDtcActionInjectsHeaderPerModuleInOrder() throws IOException, InterruptedException {
		// Exact-sequence check (mirrors CANMessageHeaderMultiplexerTest's style) that
		// ExecutionContext.scheduleDTCAction emits "ATSH<header>" immediately before each module's
		// DTC read command, in the order the modules were given. A real PID is queried (an empty
		// Query makes CommandsSuplier log a "target command list is empty" error which can trigger
		// a reconnect under load, corrupting the recorded sequence with a stray init handshake) -
		// regular polling may legitimately interleave before the DTC batch starts, so this anchors
		// on the batch's first command rather than assuming nothing else was recorded first.
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		DataCollector collector = new DataCollector();
		Workflow workflow = getWorkflow(lifecycle, collector, "en", "giulia_2.0_gme.json");

		Query query = Query.builder().pid(6L).build();

		MockAdapterConnection connection = createBaseConnection()
				.requestResponse("19020D", "00F0:5902CF26E4001:482BC10048D0082:00480")
				.build();

		workflow.start(connection, query, createDefaultInit(), createAdjustments(false, false, false));
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();

		final List<Header> modules = Arrays.asList(
				Header.builder().mode("Engine").header("7E0").build(),
				Header.builder().mode("ABS").header("760").build(),
				Header.builder().mode("Airbag").header("18DA1AF1").build());

		workflow.scheduleDTCAction(Collections.singleton(DtcAction.READ), modules);
		WorkflowFinalizer.finalizeAfter(workflow, 800);

		final BlockingDeque<String> recordedQueries = connection.recordedQueries();

		String next = recordedQueries.pop();
		while (!"ATSH7E0".equals(next)) {
			next = recordedQueries.pop();
		}

		Assertions.assertThat(recordedQueries.pop()).isEqualTo("19020D");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSH760");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("19020D");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSH18DA1AF1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("19020D");
	}

	@ParameterizedTest
	@CsvSource({
		"en, Test Failed, Test Failed This Operation Cycle",
		"pl, Test zakończony niepowodzeniem, Test nieudany w bieżącym cyklu pracy"
	})
	public void scheduleDtcSnapshotsRead(String languageCode, String expectedStatus1, String expectedStatus2) throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		DataCollector collector = new DataCollector();
		Workflow workflow = getWorkflow(lifecycle, collector, languageCode, "alfa.json");

		Query query = Query.builder().pid(6L).build();

		MockAdapterConnection connection = buildSnapshotConnection();
		
		WorkflowExecutionStatus status = workflow.start(connection, query, createDefaultInit(), createAdjustments(false, false, false));
		WorkflowMonitor.waitUntilRunning(workflow);
		
		Assertions.assertThat(workflow.isRunning()).isTrue();
		Assertions.assertThat(status).isEqualTo(WorkflowExecutionStatus.STARTED);

		workflow.scheduleDTCAction(Sets.newHashSet(DtcAction.READ_SNAPSHPOTS));
		WorkflowFinalizer.finalizeAfter(workflow, 1200);

		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		final List<DiagnosticTroubleCode> dtcList = new ArrayList<>(lifecycle.getReceivedDtc());
		Assertions.assertThat(dtcList).hasSize(18);
		
		Assertions.assertThat(dtcList)
				.contains(createDtc("P0191", "13", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0685", "11", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("U1601", "87", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("U1706", "87", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("U1700", "92", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("U1702", "87", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0121", "14", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0190", "17", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0120", "14", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0220", "14", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0621", "15", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0100", "1C", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0230", "15", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0105", "15", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0235", "15", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0115", "15", DtcDictionary.UNKNOWN_DTC))
				.contains(createDtc("P0500", "65", DtcDictionary.UNKNOWN_DTC));
		
		dtcList.forEach(dtc -> Assertions.assertThat(dtc.getSnapshot()).isNotNull().hasSize(11));
		Assertions.assertThat(dtcList.get(0).getActiveStatuses()).contains(expectedStatus1, expectedStatus2);
	}
	
	@Test
	public void dtcReadConfigurationDisabled() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();
		DataCollector collector = new DataCollector();
		Workflow workflow = getWorkflow(lifecycle, collector, "en", "mode01.json", "giulia_2.0_gme.json");

		Query query = Query.builder().pid(6L).pid(12L).pid(13L).pid(16L).pid(18L).pid(14L).build();

		MockAdapterConnection connection = createBaseConnection()
				.requestResponse("19020D", "00F0:5902CF26E4001:482BC10048D0082:00480")
				.build();
		
		workflow.start(connection, query, createDefaultInit(), createAdjustments(false, true, false));

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		WorkflowFinalizer.finalize(workflow);

		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		Assertions.assertThat(lifecycle.getDtc()).isEmpty();
	}

	// --- Helper Methods for Configuration ---

	private MockAdapterConnection buildSnapshotConnection() {
		return createBaseConnection()
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
	}

	// WorkflowMonitor.waitUntilRunning gives up after ~300ms, shorter than the script engine
	// start-up on a cold JVM; this waits long enough to be deterministic.
	// A scheduled clear used to be reported as NO_DATA whatever the ECU answered, so an app could not
	// tell a refused clear (e.g. 7F 14 22, conditions not correct while the engine runs) from a
	// successful one. "54" is the positive reply of giulia_2.0_gme.json's DTC clear.
	@ParameterizedTest
	@CsvSource(value = {
			"54,true,OK",
			"7F1422,true,ERR",
			"54,false,OK",
			"7F1422,false,ERR"
	})
	public void scheduledClearReportsTheClearStatus(String clearReply, boolean withModules,
			DiagnosticTroubleCodeClearStatus expected) throws IOException, InterruptedException {
		final ClearStatusLifecycle lifecycle = new ClearStatusLifecycle();
		final Workflow workflow = getWorkflow(lifecycle, new DataCollector(), "en", "giulia_2.0_gme.json");

		final MockAdapterConnection connection = createBaseConnection()
				.requestResponse("14FFFFFF", clearReply)
				.requestResponse("19020D", "5902CF")
				.build();

		workflow.start(connection, Query.builder().pid(6L).build(), createDefaultInit(), createAdjustments(false, false, false));
		awaitRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();

		final List<Header> modules = withModules ? Arrays.asList(
				Header.builder().mode("Engine").header("7E0").build(),
				Header.builder().mode("ABS").header("760").build()) : Collections.emptyList();

		workflow.scheduleDTCAction(Sets.newHashSet(DtcAction.CLEAR, DtcAction.READ), modules);
		WorkflowFinalizer.finalizeAfter(workflow, 4000, () -> lifecycle.clearStatus != null);

		Assertions.assertThat(lifecycle.clearStatus).isEqualTo(expected);
	}

	@Test
	public void scheduledReadReportsNoClearStatus() throws IOException, InterruptedException {
		final ClearStatusLifecycle lifecycle = new ClearStatusLifecycle();
		final Workflow workflow = getWorkflow(lifecycle, new DataCollector(), "en", "giulia_2.0_gme.json");

		final MockAdapterConnection connection = createBaseConnection()
				.requestResponse("14FFFFFF", "54")
				.requestResponse("19020D", "5902CF")
				.build();

		workflow.start(connection, Query.builder().pid(6L).build(), createDefaultInit(), createAdjustments(false, false, false));
		awaitRunning(workflow);

		workflow.scheduleDTCAction(Collections.singleton(DtcAction.READ),
				Collections.singletonList(Header.builder().mode("Engine").header("7E0").build()));
		WorkflowFinalizer.finalizeAfter(workflow, 4000, () -> lifecycle.clearStatus != null);

		Assertions.assertThat(lifecycle.clearStatus).isEqualTo(DiagnosticTroubleCodeClearStatus.NO_DATA);
	}

	// With DTC reading on connect enabled, the init read's codes (default "ecu" module) were left in
	// DiagnosticTroubleCodeHandler's accumulator and handed out with the first scheduled read, so a
	// multi-module scan listed them a second time under an untagged module.
	@Test
	public void scheduledReadDoesNotReturnCodesFromTheInitRead() throws IOException, InterruptedException {
		final SimpleLifecycle lifecycle = new SimpleLifecycle();
		final Workflow workflow = getWorkflow(lifecycle, new DataCollector(), "en", "giulia_2.0_gme.json");

		final MockAdapterConnection connection = createBaseConnection()
				.requestResponse("19020D", "7F197804B0:5902CF0191131:8F068511CDD6012:870FD706870FD73:00920FD702870F4:0121148F0221145:8F0190170F01206:148F0220148F067:21150F01001C0F8:0230158F0105159:0F0235158F0115A:158F0500640F")
				.build();

		workflow.start(connection, Query.builder().pid(6L).build(), createDefaultInit(200), createAdjustments(true, false, false));
		awaitRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		// The init read happened (reported with onRunning, which may lag isRunning).
		final long deadline = System.currentTimeMillis() + 5000;
		while (initDtc(lifecycle).isEmpty() && System.currentTimeMillis() < deadline) {
			Thread.sleep(20);
		}
		Assertions.assertThat(initDtc(lifecycle)).isNotEmpty();

		workflow.scheduleDTCAction(Collections.singleton(DtcAction.READ),
				Arrays.asList(Header.builder().mode("Engine").header("7E0").build(),
						Header.builder().mode("ABS").header("760").build()));
		WorkflowFinalizer.finalizeAfter(workflow, 4000, () -> lifecycle.getReceivedDtc() != null);

		Assertions.assertThat(lifecycle.getReceivedDtc()).isNotEmpty();
		Assertions.assertThat(lifecycle.getReceivedDtc())
				.extracting(DiagnosticTroubleCode::getModule)
				.containsOnly("Engine", "ABS");
	}

	private static Set<DiagnosticTroubleCode> initDtc(SimpleLifecycle lifecycle) {
		try {
			return lifecycle.getDtc() == null ? Collections.emptySet() : lifecycle.getDtc();
		} catch (NullPointerException e) {
			// SimpleLifecycle delegates to the capabilities, unset before onRunning.
			return Collections.emptySet();
		}
	}

	private static final class ClearStatusLifecycle implements Lifecycle {
		private volatile DiagnosticTroubleCodeClearStatus clearStatus;

		@Override
		public void onDTCCompleted(Set<DiagnosticTroubleCode> dtc, DiagnosticTroubleCodeClearStatus status) {
			this.clearStatus = status;
		}
	}

	private static void awaitRunning(Workflow workflow) throws InterruptedException {
		final long deadline = System.currentTimeMillis() + 5000;
		while (!workflow.isRunning() && System.currentTimeMillis() < deadline) {
			Thread.sleep(20);
		}
	}

	private MockAdapterConnection.MockAdapterConnectionBuilder createBaseConnection() {
		return MockAdapterConnection.builder()
				.requestResponse("22F194", "00E0:62F1945031341:315641304520202:20")
				.requestResponse("221008", "6210080000BFC8")
				.requestResponse("222008", "6220080000BFC7")
				.requestResponse("22F195", "62F1950000")
				.requestResponse("22F193", "62F19300")
				.requestResponse("0100", "4100be3ea813")
				.requestResponse("0200", "4140fed00400")
				.requestResponse("0105", "410522")
				.requestResponse("010C", "410c541B")
				.requestResponse("010B", "410b35");
	}
	
	private Init createDefaultInit() {
		return createDefaultInit(0);
	}
	
	private Init createDefaultInit(final int delayAfterInit) {
		return Init.builder()
				.delayAfterInit(delayAfterInit)
				.header(Header.builder().mode("22").header("DA10F1").build())
				.header(Header.builder().mode("01").header("DB33F1").build())
				.protocol(Protocol.CAN_29)
				.sequence(DefaultCommandGroup.INIT)
				.build();
	}

	private Adjustments createAdjustments(boolean dtcEnabled, boolean metadataEnabled, boolean debugEnabled) {
		return Adjustments.builder()
				.debugEnabled(debugEnabled)
				.vehicleDtcReadingEnabled(dtcEnabled)
				.vehicleMetadataReadingEnabled(metadataEnabled)
				.vehicleCapabilitiesReadingEnabled(metadataEnabled)	
				.cachePolicy(CachePolicy.builder()
						.storeResultCacheOnDisk(false)
						.resultCacheEnabled(false).build())
				.adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy.builder()
						.enabled(false)
						.commandFrequency(6)
						.build())
				.producerPolicy(ProducerPolicy.builder()
						.priorityQueueEnabled(true)
						.build())
				.batchPolicy(BatchPolicy.builder().enabled(true).build())
				.build();
	}

	private DiagnosticTroubleCode createDtc(String code, String status, String description) {
		// Decoded DTCs are tagged with PidDefinition's default module ("ecu") when no explicit
		// module/header is set, and module is part of DiagnosticTroubleCode's equals()/hashCode().
		final DiagnosticTroubleCode dtc = new DiagnosticTroubleCode(code, status, null, description, 0, null, null, null, null, null);
		dtc.setModule("ecu");
		return dtc;
	}
}