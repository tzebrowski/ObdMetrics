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
import org.obd.metrics.api.model.Query;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.test.utils.MockAdapterConnection;
import org.obd.metrics.test.utils.SimpleLifecycle;
import org.obd.metrics.test.utils.SimpleWorkflowFactory;
import org.obd.metrics.test.utils.WorkflowFinalizer;

public class ConnectorTest {

	@Test
	public void characterTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		Query query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("09 02", "SEARCHING...0140:4902015756571:5A5A5A314B5A412:4D363930333932")
		        .requestResponse("0100", "\r4100be3ea813")
		        .requestResponse("0200", "4140fed00400\n")
		        .requestResponse("0115", "\t4 1 1 5 F F f f>\r")
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalize(workflow);

		Assertions.assertThat(lifecycle.isErrorOccurred()).isFalse();

		PidDefinition findBy = workflow.getPidRegistry().findBy(22l);
		Assertions.assertThat(findBy).isNotNull();
		
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, findBy)
		        .get().getValue();

		Assertions.assertThat(ratePerSec).isGreaterThan(0);
	}

	@Test
	public void readErrorTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		Query query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("0115", "4115FFff")
		        .simulateReadError(true) // simulate read error
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalize(workflow);

		Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
	}

	@Test
	public void reconnectErrorTest() throws IOException, InterruptedException {
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle);

		Query query = Query.builder()
		        .pid(22l)
		        .pid(23l)
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("0115", "4115FFff")
		        .simulateErrorInReconnect(true)
		        .simulateWriteError(true) // simulate write error
		        .build();

		workflow.start(connection, query);

		WorkflowFinalizer.finalize(workflow);

		Assertions.assertThat(lifecycle.isErrorOccurred()).isTrue();
	}
}
