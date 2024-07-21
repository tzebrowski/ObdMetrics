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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.UnitsConversionPolicy;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;

public class UnitsConversionTest {

	@ParameterizedTest
	@CsvSource(value = { 
			"62100170=64=28.1", 
			"62100170=58=25.5", }, delimiter = '=')
	public void tankSizeTest(String ecuReply, int tankSize, double doubleExpectedValue)
			throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), collector, "giulia_2.0_gme.json");

		final long pid = 7040l;
		final Query query = Query.builder().pid(pid).build();

		// Create an instance of mock connection with additional commands and replies
		final String queryStr = "22 1001";
		final MockAdapterConnection connection = MockAdapterConnection.builder().requestResponse(queryStr, ecuReply)
				.build();

		// Enabling batch commands
		final Adjustments optional = Adjustments.builder()
				.debugEnabled(Boolean.FALSE)
				.vehicleCapabilitiesReadingEnabled(Boolean.FALSE)
				.vehicleDtcCleaningEnabled(Boolean.FALSE)
				.vehicleMetadataReadingEnabled(Boolean.FALSE)
				.vehicleDtcReadingEnabled(Boolean.FALSE)
				.unitsConversionPolicy(UnitsConversionPolicy.builder().param("unit_tank_size",tankSize).build())
				.cachePolicy(CachePolicy.builder().storeResultCacheOnDisk(Boolean.FALSE)
						.resultCacheEnabled(Boolean.FALSE).build())
				.adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy.builder().enabled(Boolean.FALSE).build())
				.producerPolicy(ProducerPolicy.builder().priorityQueueEnabled(Boolean.TRUE).build())
				.batchPolicy(BatchPolicy.builder().responseLengthEnabled(Boolean.FALSE).enabled(Boolean.TRUE).build())
				.build();

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, optional);

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 500);

		// Ensure batch commands were sent out
		Assertions.assertThat(connection.recordedQueries()).contains(queryStr);

		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();

		ObdMetric metric = collector.findSingleMetricBy(workflow.getPidRegistry().findBy(pid));
		Assertions.assertThat(metric).isNotNull();

		Assertions.assertThat(metric.getValue()).isInstanceOf(Double.class);
		Assertions.assertThat(metric.getValue()).isEqualTo(doubleExpectedValue);
	}
}
