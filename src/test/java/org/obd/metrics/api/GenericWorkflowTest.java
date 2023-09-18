/** 
 * Copyright 2019-2023, Tomasz Żebrowski
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
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.connection.MockAdapterConnection;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;


public class GenericWorkflowTest {

	@Test
	public void recieveReplyTest() throws IOException, InterruptedException {
		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Create an instance of the Mode 22 Workflow
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		// Query for specified PID's like RPM
		Query query = Query.builder()
		        .pid(6008l) // Coolant
		        .pid(6004l) // RPM
		        .pid(6007l) // Intake temp
		        .pid(6015l)// Oil temp
		        .pid(6003l) // Spark Advance
		        .build();

		// Create an instance of mocked connection with additional commands and replies
		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("221003", "62100340")
		        .requestResponse("221000", "6210000BEA")
		        .requestResponse("221935", "62193540")
		        .requestResponse("22194f", "62194f2d85")
		        .build();

		// Extra settings for collecting process like command frequency 14/sec
		Adjustments optional = Adjustments.builder()
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(20)// 20ms
		                .commandFrequency(14).build())
		        .producerPolicy(ProducerPolicy.builder().priorityQueueEnabled(false).build())
		        .build();

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, optional);

		PidDefinition rpm = workflow.getPidRegistry().findBy(6004l);

		// Workflow completion thread, it will end workflow after some period of time
		// (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 3000, ()-> workflow.getDiagnostics().rate().findBy(RateType.MEAN,rpm).get().getValue() > 5);
		
		// Ensure we receive AT command as well
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		// Gets the metric
		ObdMetric metric = collector.findSingleMetricBy(rpm);
		Assertions.assertThat(metric).isNotNull();
		Assertions.assertThat(metric.valueToDouble()).isEqualTo(762.5);
	}
}
