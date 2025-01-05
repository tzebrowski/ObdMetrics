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
package org.obd.metrics.codec.custom;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;


public class CustomDecoderTest {
	
	@ParameterizedTest
	@CsvSource(value = { 
			"00C0:6219210100001:000000000100=010000000000000100",//mar off
			"00C0:6219210400001:000000000400=040000000000000400",//mar on
			"00C0:6219214000011:000000000400=400001000000000400",//running
			
	}, delimiter = '=')
	public void test(String message,String expectedValue) throws IOException, InterruptedException {
		// Specify lifecycle observer
		SimpleLifecycle lifecycle = new SimpleLifecycle();

		// Specify the metrics collector
		DataCollector collector = new DataCollector();

		Workflow workflow = SimpleWorkflowFactory.getWorkflow(lifecycle, collector, "test_resource.json");
		
		final long pid = 1111L;
		Query query = Query.builder()
		        .pid(pid) 
		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("STPX H:18DA10F1, D:22 1921, R:1", message)
		        .build();
		
		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("18DA10F1").build())
				.protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
			
		final Adjustments optional = adjustements();
		
		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 800);

		// Ensure we receive AT command
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		
		final PidDefinition pidDef = workflow.getPidRegistry().findBy(pid);
		Assertions.assertThat(pidDef).isNotNull();
		ObdMetric findSingleMetricBy = collector.findSingleMetricBy(pidDef);
		
		Assertions.assertThat(findSingleMetricBy).isNotNull();
		final String value = (String)findSingleMetricBy.getValue();
		Assertions.assertThat(value).isNotNull();
		
		
		Assertions.assertThat(value).isEqualTo(expectedValue);
	}
	
	private Adjustments adjustements() {
		return Adjustments
		        .builder()
		        .debugEnabled(Boolean.TRUE)
		        .vehicleMetadataReadingEnabled(Boolean.TRUE)
		        .vehicleCapabilitiesReadingEnabled(Boolean.TRUE)	
		        .stNxx(STNxxExtensions.builder().enabled(Boolean.TRUE).build())
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
	}
}
