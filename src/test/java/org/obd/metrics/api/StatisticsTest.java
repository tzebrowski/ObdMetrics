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
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.diagnostic.Histogram;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;

public class StatisticsTest {

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
	public void genericWorkflowTest() throws IOException, InterruptedException {

		DataCollector collector = new DataCollector();
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		Query query = Query.builder()
		        .pid(6008l) // Coolant
		        .pid(6004l) // RPM
		        .pid(6007l) // Intake temp
		        .pid(6015l)// Oil temp
		        .pid(6003l) // Spark Advance
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("221003", "62100340")
		        .requestResponse("221000", "6210000BEA")
		        .requestResponse("221935", "62193540")
		        .requestResponse("22194f", "62194f2d85")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalize(workflow);

		PidDefinitionRegistry pids = workflow.getPidRegistry();

		PidDefinition pid8l = pids.findBy(6008l);
		Histogram stat8l = workflow.getDiagnostics().histogram().findBy(pid8l);
		Assertions.assertThat(stat8l).isNotNull();

		PidDefinition pid4l = pids.findBy(6004l);
		Histogram stat4L = workflow.getDiagnostics().histogram().findBy(pid4l);
		Assertions.assertThat(stat4L).isNotNull();

		Assertions.assertThat(stat4L.getMax()).isEqualTo(762);
		Assertions.assertThat(stat4L.getMin()).isEqualTo(762);
		Assertions.assertThat(stat4L.getMean()).isEqualTo(762);
		Assertions.assertThat(stat4L.getLatestValue()).isEqualTo(762);

		Assertions.assertThat(stat8l.getMax()).isEqualTo(0);
		Assertions.assertThat(stat8l.getMin()).isEqualTo(0);
		Assertions.assertThat(stat8l.getMean()).isEqualTo(0);
		Assertions.assertThat(stat8l.getLatestValue()).isEqualTo(0);

		Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN, pid8l).get().getValue()).isGreaterThan(5d);
		Assertions.assertThat(workflow.getDiagnostics().rate().findBy(RateType.MEAN, pid4l).get().getValue()).isGreaterThan(5d);
	}
}
