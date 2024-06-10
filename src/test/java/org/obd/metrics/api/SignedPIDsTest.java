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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;

public class SignedPIDsTest {
	
	@ParameterizedTest
	@CsvSource(value = { 
			"6055=22197D=62197DF9D0=-12.375",
			"6056=22197B=62197BE000=-64",
	}, delimiter = '=')
	public void nonBatch(Long id,String req, String resp, Double expectedValue) throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Getting the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);

		Query query = Query.builder()
		        .pid(id) 
		        .build();

		// Create an instance of mock connection with additional commands and replies
		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse(req, resp)
		        .build();

		final Adjustments optional = Adjustments
		        .builder()
		        .debugEnabled(Boolean.TRUE)
		        .cachePolicy(
		        		CachePolicy.builder()
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .checkInterval(5)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchPolicy(BatchPolicy.builder().enabled(Boolean.FALSE).build())
		        .build();

		workflow.start(connection, query, optional);

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		
		WorkflowFinalizer.finalizeAfter(workflow, 500);

		Assertions.assertThat(connection.recordedQueries())
			.contains(req);

		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		validateExpectedValue(collector, workflow, id, expectedValue);
	}
	
	
	@Test
	public void batchEnabled() throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Getting the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);
		//D:22 196D 196A 197B 196C 1970 197C 197D 181C
		
		final Query query = Query.builder()
		        .pid(6038l)
		        .pid(6068l)
		        .pid(6056l)
		        .pid(6037l)
		        .pid(6069l)
		        .pid(6059l)
		        .pid(6055l)
		        .pid(6049l)
		        .pid(6058l)
		        .pid(6050l)
		        .pid(6058l)

		        .build();

		MockAdapterConnection connection = MockAdapterConnection.builder()
		        .requestResponse("STPX H:18DA10F1, D:22 196D 196A 197B 196C 1970 197C 197D 181C", 
		        		"01D0:62196D0A24191:6A1000197B00002:196C0A001970103:00197C0000197D4:0000")
		        .requestResponse("STPX H:18DA10F1, D:22 197A 181B 196E","0090:62197A0000191:6E0000")
		        .build();

		final Adjustments optional = Adjustments
		        .builder()
		        .debugEnabled(Boolean.TRUE)
		        .cachePolicy(
		        		CachePolicy.builder()
		        		.resultCacheEnabled(Boolean.FALSE).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .checkInterval(5)
		                .commandFrequency(6)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .stNxx(STNxxExtensions.builder().enabled(Boolean.TRUE).build())
		        .batchPolicy(BatchPolicy
		        		.builder()
		        		.responseLengthEnabled(Boolean.FALSE)
		        		.mode22BatchSize(8)
		        		.enabled(Boolean.TRUE).build())
		        .build();

		final Init init = Init.builder()
		        .delayAfterInit(0)
		        .header(Header.builder().mode("22").header("18DA10F1").build())
				.header(Header.builder().mode("01").header("18DB33F1").build())
				.header(Header.builder().mode("14").header("18DA10F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
		
		workflow.start(connection, query, init, optional);

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		
		WorkflowFinalizer.finalizeAfter(workflow, 900);

		Assertions.assertThat(connection.recordedQueries())
			.contains("STPX H:18DA10F1, D:22 196D 196A 197B 196C 1970 197C 197D 181C");

	
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
		validateExpectedValue(collector, workflow, 6038, 20.3);
	}


	private void validateExpectedValue(DataCollector collector, Workflow workflow, final long id,
			final double expectedValue) {
		
		final PidDefinition pidDefinition = workflow.getPidRegistry().findBy(id);

		ObdMetric metric = collector.findSingleMetricBy(pidDefinition);
		Assertions.assertThat(metric).isNotNull();
		Assertions.assertThat(metric.getValue()).isEqualTo(expectedValue);
	}
	
}
