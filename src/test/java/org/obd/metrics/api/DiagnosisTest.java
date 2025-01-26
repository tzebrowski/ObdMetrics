 /**
 * Copyright 2019-2025, Tomasz Żebrowski
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
import org.obd.metrics.api.model.Query;
import org.obd.metrics.connection.MulitAnswerMockAdapterConnection;
import org.obd.metrics.diagnostic.Histogram;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;

public class DiagnosisTest {

	@Test
	public void mode01WorkflowTest() throws IOException, InterruptedException {

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
		        .requestResponse("01 0B 0C 11 0D 05 0F 3", "00E0:410BFF0C00001:11000D0005000F2:00AAAAAAAAAAAA")
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

		MulitAnswerMockAdapterConnection connection = MulitAnswerMockAdapterConnection.builder()
				.requestResponse("221003", List.of("62100340","62100336","621003C0"))
		        .requestResponse("221000", List.of("6210000BEA","62100055FF"))
		        .requestResponse("221935", List.of("62193550","621935AA"))
		        .build();

		workflow.start(connection, query,Adjustments.builder().debugEnabled(false).build());

		WorkflowFinalizer.finalizeAfter(workflow,800);

		PidDefinitionRegistry pids = workflow.getPidRegistry();

		{
			PidDefinition rpmPid = pids.findBy(rpmId);
			Histogram rpmStats = workflow.getDiagnostics().histogram().findBy(rpmPid);
			Assertions.assertThat(rpmStats).isNotNull();
			Assertions.assertThat(rpmStats.getMax()).isEqualTo(5503);
			Assertions.assertThat(rpmStats.getMin()).isEqualTo(762);
			Assertions.assertThat(rpmStats.getMean()).isGreaterThan(0);
			Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN, rpmPid).get().getValue()).isGreaterThan(5d);

		}
		
		{
			PidDefinition coolantPid = pids.findBy(coolantId);
			Histogram coolantStats = workflow.getDiagnostics().histogram().findBy(coolantPid);
			Assertions.assertThat(coolantStats).isNotNull();
			Assertions.assertThat(coolantStats.getMax()).isEqualTo(96.0);
			Assertions.assertThat(coolantStats.getMin()).isEqualTo(-7.0);
			Assertions.assertThat(coolantStats.getMean()).isGreaterThan(0);
			Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN, coolantPid).get().getValue()).isGreaterThan(5d);
		}
		
		
		{
			PidDefinition mafTempPid = pids.findBy(mafTempId);
			Histogram mafTempStats = workflow.getDiagnostics().histogram().findBy(mafTempPid);
			Assertions.assertThat(mafTempStats).isNotNull();
			Assertions.assertThat(mafTempStats.getMax()).isEqualTo(80.0);
			Assertions.assertThat(mafTempStats.getMin()).isEqualTo(12.0);
			Assertions.assertThat(mafTempStats.getMean()).isGreaterThan(0);
			Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN, mafTempPid).get().getValue()).isGreaterThan(5d);
		}
	}
}
