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
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.test.utils.DataCollector;
import org.obd.metrics.test.utils.MockAdapterConnection;
import org.obd.metrics.test.utils.SimpleWorkflowFactory;
import org.obd.metrics.test.utils.WorkflowFinalizer;
import org.obd.metrics.test.utils.WorkflowMonitor;

public class AdaptiveTimingTest {

	@Test
	public void adaptiveTimeoutPolicyTest() throws IOException, InterruptedException {

		//Create an instance of DataCollector that receives the OBD Metrics
		final DataCollector collector = new DataCollector();

		//Getting the Workflow instance for mode 22
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);
		
		//Query for specified PID's like: Engine coolant temperature
		final Query query = Query.builder()
		        .pid(6008l) // Coolant
		        .pid(6004l) // RPM
		        .pid(6007l) // Intake temp
		        .pid(6015l)// Oil temp
		        .pid(603l) // Spark Advance
		        .build();

		//Create an instance of mock connection with additional commands and replies 
		final MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("221003", "62100340")
		        .requestResponse("221000", "6210000BEA")
		        .requestResponse("221935", "62193540")
		        .requestResponse("22194f", "62194f2d85")
		// Set read timeout for every character,e.g: inputStream.read(), we want to ensure that initial timeout will be decrease during the tests			        
		        .readTimeout(1) //
		        .build();
		
		// Set target frequency
		final int targetCommandFrequency = 4;

		// Enable adaptive timing
		final Adjustments optional = Adjustments
		        .builder()
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(10)// 20ms
		                .commandFrequency(targetCommandFrequency)
		                .build())
		        .build();

		//Start background threads, that call the adapter,decode the raw data, and populates OBD metrics
		workflow.start(connection, query,Init.DEFAULT,optional);

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		
		PidDefinition rpm = workflow.getPidRegistry().findBy(6004l);

		// Starting the workflow completion job, it will end workflow after some period of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 500, ()-> workflow.getDiagnostics().rate().findBy(RateType.MEAN,rpm).get().getValue() > targetCommandFrequency + 2);
		
		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		// Ensure target command frequency is on the expected level
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN,rpm).get().getValue();
		Assertions.assertThat(ratePerSec)
		        .isGreaterThanOrEqualTo(targetCommandFrequency);
	}
}
