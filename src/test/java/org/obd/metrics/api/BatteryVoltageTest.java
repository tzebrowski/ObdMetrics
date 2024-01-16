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
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.connection.MockAdapterConnection;
import org.obd.metrics.pid.PidDefinition;

public class BatteryVoltageTest {

	@Test
	public void case_01() throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector(true);

		
		// Query for specified PID's like: Engine coolant temperature
		Query query = Query.builder()
		        .pid(6l) // Engine coolant temperature
		        .pid(9000l) // Battery voltage
		        .build();

		// Create an instance of mock connection with additional commands and replies
		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("ATRV", "14.1v")
		        .requestResponse("0100", "4100be3ea813")
		        .requestResponse("0200", "4140fed00400")
		        .requestResponse("01 0B 0C 11 0D 0F 05", "00e0:410bff0c00001:11000d000f00052:00aaaaaaaaaaaa").build();

		// Enabling batch commands
		Adjustments optional = Adjustments
		        .builder()
		        .vehicleMetadataReadingEnabled(Boolean.FALSE)
				.vehicleCapabilitiesReadingEnabled(Boolean.FALSE)
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy.builder().enabled(false).build())
		        .cachePolicy(CachePolicy.builder().resultCacheEnabled(Boolean.FALSE).build())
		        .producerPolicy(ProducerPolicy.builder().priorityQueueEnabled(false).build())
		        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
		        .build();

		Init init = Init.builder()
				.delayAfterInit(0)
				.build();
		
		// Getting the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(),collector,"mode01.json","extra.json");
		
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 400);

		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		final PidDefinition batteryVoltagePid = workflow.getPidRegistry().findBy(9000l);
		// Ensure pid was loaded from extra.json file
		Assertions.assertThat(batteryVoltagePid).overridingErrorMessage("no battery voltage pid found").isNotNull();
		
		// Find metrics for battery voltage
		final ObdMetric metric = collector.findSingleMetricBy(batteryVoltagePid);

		Assertions.assertThat(metric).overridingErrorMessage("no battery voltage metrics found").isNotNull();
		Assertions.assertThat(metric.getValue()).isInstanceOf(Double.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(14.1);
	}
}
