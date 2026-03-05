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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.diagnostic.Histogram;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;
import org.obd.metrics.transport.mock.SmartMockAdapterConnection;

public class DiagnosisTest {

	@Test
	public void rateTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(12l) // Intake manifold absolute pressure
		        .pid(13l) // Engine RPM
		        .pid(16l) // Intake air temperature
		        .pid(18l) // Throttle position
		        .pid(14l) // Vehicle speed
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("01 0B 0C 11 0D 05 0F 2", "00E0:410BFF0C00001:11000D0005000F2:00AAAAAAAAAAAA")
		        .build();
		
		Adjustments optional = Adjustments.builder()
				.batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
		        .build();

		workflow.start(connection, query, optional);

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		WorkflowFinalizer.finalize(workflow);


		PidDefinitionRegistry pids = workflow.getPidRegistry();

		PidDefinition engineTemp = pids.findBy(6l);
		Assertions.assertThat(engineTemp.getPid()).isEqualTo("05");

		Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN, engineTemp).get().getValue()).isGreaterThan(5);
	
		Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN, pids.findBy(12l)).get().getValue()).isGreaterThan(5d);
	}

	@Test
	public void multiValueTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		final long rpmId = 6004l;
		final long coolantId = 6008l;
		final long mafTempId = 6007l;
		Query query = Query.builder()
		        .pid(coolantId) // Coolant
		        .pid(rpmId) // RPM
		        .pid(mafTempId) // Intake temp
		        .build();

		SmartMockAdapterConnection connection = SmartMockAdapterConnection.defaultBuilder()
				.requestResponse("221003", List.of("62100340","62100336","621003C0"))
		        .requestResponse("221000", List.of("6210000BEA","62100055FF"))
		        .requestResponse("221935", List.of("62193550","621935AA"))
		        .build();

		workflow.start(connection, query,Adjustments.builder().debugEnabled(false).build());

		WorkflowFinalizer.finalizeAfter(workflow,800);

		assertHistogram(workflow, rpmId, 5503.0, 762.0);
		assertHistogram(workflow, coolantId, 96.0, -7.0);
		assertHistogram(workflow, mafTempId, 80.0, 12.0);
	}
	

	@Test
	public void nonNumberValueTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), collector, 
				"test_resource.json","mode01.json");

		final long customPidId = 1111l;
		final Query query = Query.builder().pid(customPidId).build();

		final SmartMockAdapterConnection connection = SmartMockAdapterConnection.defaultBuilder()
				.requestResponse("22 1921 2", List.of("00C0:6219210100001:000000000100"))
				.requestResponse("0100", List.of("4100be3ea813"))
		        .requestResponse("0200", List.of("4140fed00400"))
		        .requestResponse("0105", List.of("410522"))
		        .requestResponse("010C", List.of("410c541B"))
		        .requestResponse("010B", List.of("410b35"))
				.build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder()
		        		.mode("22").header("DA10F1").build())
				.header(Header.builder()
						.mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();

		workflow.start(connection, query, init, Adjustments
				.builder()
				.debugEnabled(true)
				.cachePolicy(CachePolicy.builder().resultCacheEnabled(false).build())
				.vehicleCapabilitiesReadingEnabled(Boolean.TRUE).build());

		WorkflowFinalizer.finalizeAfter(workflow, 800);

		assertHistogram(workflow, customPidId, 0.0, 0.0, false);
	}
	
	
	@Test
	public void mixTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), collector, 
				"test_resource.json", "mode01.json", "alfa.json");

		final long customPidId = 1111l;
		final long rpmId = 6004l;
		final long coolantId = 6008l;
		final long mafTempId = 6007l;
		Query query = Query.builder()
		        .pid(coolantId) // Coolant
		        .pid(rpmId) // RPM
		        .pid(mafTempId) // Intake temp
		        .pid(customPidId)
		        .build();

		final SmartMockAdapterConnection connection = SmartMockAdapterConnection.defaultBuilder()
				.requestResponse("221003", List.of("62100340","62100336","621003C0"))
		        .requestResponse("221000", List.of("6210000BEA","62100055FF"))
		        .requestResponse("221935", List.of("62193550","621935AA"))
				.requestResponse("22 1921 2", List.of("00C0:6219210100001:000000000100"))
				.requestResponse("0100", List.of("4100be3ea813"))
		        .requestResponse("0200", List.of("4140fed00400"))
		        .requestResponse("0105", List.of("410522"))
		        .requestResponse("010C", List.of("410c541B"))
		        .requestResponse("010B", List.of("410b35"))
				.build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder()
		        		.mode("22").header("DA10F1").build())
				.header(Header.builder()
						.mode("01").header("DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();

		workflow.start(connection, query, init, Adjustments
				.builder()
				.debugEnabled(false)
				.cachePolicy(CachePolicy.builder().resultCacheEnabled(false).build())
				.vehicleCapabilitiesReadingEnabled(Boolean.TRUE).build());

		WorkflowFinalizer.finalizeAfter(workflow, 800);

		assertHistogram(workflow, customPidId, 0.0, 0.0, false);
		assertHistogram(workflow, rpmId, 5503.0, 762.0);
		assertHistogram(workflow, coolantId, 96.0, -7.0);
		assertHistogram(workflow, mafTempId, 80.0, 12.0);
	}
	
	void assertHistogram(Workflow workflow, Long pidID, Number max, Number min) {
		assertHistogram(workflow, pidID, max, min, true);
	}

	void assertHistogram(Workflow workflow, Long pidID, Number max, Number min, boolean assertMeanValue) {
		PidDefinitionRegistry pids = workflow.getPidRegistry();
		PidDefinition pid = pids.findBy(pidID);
		Histogram rpmStats = workflow.getDiagnostics().histogram().findBy(pid);
		Assertions.assertThat(rpmStats).isNotNull();
		Assertions.assertThat(rpmStats.getMax()).isEqualTo(max);
		Assertions.assertThat(rpmStats.getMin()).isEqualTo(min);
		
		if (assertMeanValue) {
			Assertions.assertThat(rpmStats.getMean()).isGreaterThan(0);
		}
		
		Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN, pid).get().getValue())
				.isGreaterThan(5d);
	}
	
}
