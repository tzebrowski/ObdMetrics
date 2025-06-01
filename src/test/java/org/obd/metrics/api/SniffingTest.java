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
import java.util.concurrent.BlockingDeque;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.SniffingPolicy;
import org.obd.metrics.api.model.SniffingPolicy.STNxxExtensions;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;

public class SniffingTest {
	
	
	@Test
	public void atmaTest() throws IOException, InterruptedException {
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), new DataCollector(false));

		final MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("ATMA","384 08 01 AC 08 00 04 02 35\n\r")
				.build();
		
		
		final SniffingPolicy sniffingPolicy = SniffingPolicy
				.builder()
				.enabled(true)
				.debugEnabled(false)
				.stNxx(STNxxExtensions
						.builder()
						.enabled(false)
						.build())
				.build();
		
		workflow.startSniffing(connection, sniffingPolicy);
		WorkflowFinalizer.finalizeAfter(workflow, 500);	
		

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATD");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATCAF0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP6");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATMA");
	}
	
	
	@Test
	public void stmaTest() throws IOException, InterruptedException {
		
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), new DataCollector(false));

		final MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("ATMA","384 08 01 AC 08 00 04 02 35\n\r")
				.build();

		
		
		final SniffingPolicy sniffingPolicy = SniffingPolicy
				.builder()
				.enabled(true)
				.debugEnabled(false)
				.stNxx(STNxxExtensions
						.builder()
						.enabled(true)
						.build())
				.build();
		
		workflow.startSniffing(connection, sniffingPolicy);
		WorkflowFinalizer.finalizeAfter(workflow, 500);	
		

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATD");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATCAF0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP6");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STMA");
	}
	
	
	@Test
	public void stnFilterTest() throws IOException, InterruptedException {
		
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), new DataCollector(false));

		final MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("ATMA","384 08 01 AC 08 00 04 02 35\n\r")
				.build();
		
		final SniffingPolicy sniffingPolicy = SniffingPolicy
				.builder()
				.enabled(true)
				.debugEnabled(false)
				.stNxx(STNxxExtensions
						.builder()
						.filter("384")
						.enabled(true)
						.build())
				.build();
		
		workflow.startSniffing(connection, sniffingPolicy);
		WorkflowFinalizer.finalizeAfter(workflow, 500);	
		

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATD");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATCAF0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("SH384");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP6");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STM");
	}
}
