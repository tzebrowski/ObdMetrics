 /**
 * Copyright 2019-2026, Tomasz Żebrowski
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
import java.util.concurrent.BlockingDeque;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.api.model.SnifferMetric;
import org.obd.metrics.api.model.SniffingPolicy;
import org.obd.metrics.api.model.SniffingPolicy.STNxxExtensions;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;

public class SniffingTest {
	
	private static final class SniffingDataCollector extends ReplyObserver<Reply<?>> {
		private final MultiValuedMap<PidDefinition, SnifferMetric> metrics = new ArrayListValuedHashMap<PidDefinition, SnifferMetric>();

		public List<SnifferMetric> findMetricsBy(PidDefinition pidDefinition) {
			return (List<SnifferMetric>) metrics.get(pidDefinition);
		}

		@Override
		public void onNext(Reply<?> reply) {
			if (reply instanceof SnifferMetric) {
				metrics.put(((SnifferMetric) reply).getCommand().getPid(), (SnifferMetric) reply);
			}
		}
	}

	
	@Test
	public void atmaTest() throws IOException, InterruptedException {
		
		final SniffingDataCollector dataCollector = new SniffingDataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), dataCollector);
		final String given = "384 08 01 AC 08 00 04 02 35\n\r";
		final MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("ATMA",given)
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
		
		workflow.start(connection, sniffingPolicy);
		WorkflowFinalizer.finalizeAfter(workflow, 500);	
		

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATCAF0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP6");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATMA");

		final List<SnifferMetric> findMetricsBy = dataCollector.findMetricsBy(workflow.getPidRegistry().findBy(Workflow.SNIFFING_PID_ID));
		Assertions.assertThat(findMetricsBy).isNotNull().isNotEmpty();
		Assertions.assertThat(findMetricsBy.get(0).getRaw().getMessage()).isNotNull().isEqualTo(given.substring(0, given.length() - 2));
	}
	
	@ParameterizedTest
	@ValueSource(strings = { 
			"STMA\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n"
			+ "BUFFER FULL\r\n",
			
			"STMA\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n",
			
			"\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n"
			+ "BUFFER FULL\r\n",
			
			
			"STMA\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n"
			+ "STOPPED\r\n",
			
			"STMA\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n",
			
			"\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n"
			+ "STOPPED\r\n"
			
		})
	public void stmaNormalizationTest(String given) throws IOException, InterruptedException {
		
		final SniffingDataCollector dataCollector = new SniffingDataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), dataCollector);

		final MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("STMA", given).build();
		
		final SniffingPolicy sniffingPolicy = SniffingPolicy
				.builder()
				.enabled(true)
				.debugEnabled(false)
				.stNxx(STNxxExtensions
						.builder()
						.enabled(true)
						.build())
				.build();
		
		workflow.start(connection, sniffingPolicy);
		WorkflowFinalizer.finalizeAfter(workflow, 1000);	
		

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATCAF0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP6");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STMA");
		
		final String expected = "\r\n" 
				+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
				+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
				+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
				+ "0A7 E";
		final List<SnifferMetric> findMetricsBy = dataCollector.findMetricsBy(workflow.getPidRegistry().findBy(Workflow.SNIFFING_PID_ID));
		Assertions.assertThat(findMetricsBy).isNotNull().isNotEmpty();
		Assertions.assertThat(findMetricsBy.get(0).getRaw().getMessage()).isNotNull().isEqualTo(expected);
	}
	
	@ParameterizedTest
	@ValueSource(strings = { 
			"ATMA\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n"
			+ "BUFFER FULL\r\n",
			
			"ATMA\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n",
			
			"\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n"
			+ "BUFFER FULL\r\n",
			
			"ATMA\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n"
			+ "STOPPED\r\n",
			
			"ATMA\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n",
			
			"\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n"
			+ "STOPPED\r\n"
				
		})
	public void atmaNormalizationTest(String given) throws IOException, InterruptedException {
		
		final SniffingDataCollector dataCollector = new SniffingDataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), dataCollector);

		final MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("ATMA", given).build();
		
		final SniffingPolicy sniffingPolicy = SniffingPolicy
				.builder()
				.enabled(true)
				.debugEnabled(false)
				.stNxx(STNxxExtensions
						.builder()
						.enabled(false)
						.build())
				.build();
		
		workflow.start(connection, sniffingPolicy);
		WorkflowFinalizer.finalizeAfter(workflow, 500);	
		

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATCAF0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP6");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATMA");
		
		final String expected = "\r\n" 
				+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
				+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
				+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
				+ "0A7 E";
		final List<SnifferMetric> findMetricsBy = dataCollector.findMetricsBy(workflow.getPidRegistry().findBy(Workflow.SNIFFING_PID_ID));
		Assertions.assertThat(findMetricsBy).isNotNull().isNotEmpty();
		Assertions.assertThat(findMetricsBy.get(0).getRaw().getMessage()).isNotNull().isEqualTo(expected);
	}
	
	
	@ParameterizedTest
	@ValueSource(strings = { 
			"STM\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n"
			+ "BUFFER FULL\r\n",
			
			"STM\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n",
			
			"\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n"
			+ "BUFFER FULL\r\n",
			
			
			"STM\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n"
			+ "STOPPED\r\n",
			
			"STM\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n",
			
			"\r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
			+ "0A7 E\r\n"
			+ "STOPPED\r\n"
			
		})
	public void stmNormalizationTest(String given) throws IOException, InterruptedException {
		
		final SniffingDataCollector dataCollector = new SniffingDataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), dataCollector);

		final MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("STM", given).build();
		
		final SniffingPolicy sniffingPolicy = SniffingPolicy
				.builder()
				.enabled(true)
				.debugEnabled(false)
				.stNxx(STNxxExtensions
						.builder()
						.filter("0A7,FFF")
						.enabled(true)
						.build())
				.build();
		
		workflow.start(connection, sniffingPolicy);
		WorkflowFinalizer.finalizeAfter(workflow, 500);	
		

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATCAF0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STFAC");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STFPA 0A7,FFF");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP6");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STM");
		
		final String expected = "\r\n" 
				+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
				+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
				+ "0A7 E2 AB 9B 6E FD E9 A6 9B \r\n"
				+ "0A7 E";
		final List<SnifferMetric> findMetricsBy = dataCollector.findMetricsBy(workflow.getPidRegistry().findBy(Workflow.SNIFFING_PID_ID));
		Assertions.assertThat(findMetricsBy).isNotNull().isNotEmpty();
		Assertions.assertThat(findMetricsBy.get(0).getRaw().getMessage()).isNotNull().isEqualTo(expected);
	}
	
	
	@Test
	public void stmaTest() throws IOException, InterruptedException {
		
		final SniffingDataCollector dataCollector = new SniffingDataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), dataCollector);

		final String given = "5A8 00 00 81 10 80 C0 02 BF\n\r";
		
		final MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("STMA",given)
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
		
		workflow.start(connection, sniffingPolicy);
		WorkflowFinalizer.finalizeAfter(workflow, 500);	
		

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATCAF0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP6");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STMA");
		
		final List<SnifferMetric> findMetricsBy = dataCollector.findMetricsBy(workflow.getPidRegistry().findBy(Workflow.SNIFFING_PID_ID));
		Assertions.assertThat(findMetricsBy).isNotNull().isNotEmpty();
		Assertions.assertThat(findMetricsBy.get(0).getRaw().getMessage()).isNotNull().isEqualTo(given.substring(0, given.length() - 2));
	}
	
	
	@Test
	public void stnFilterTest() throws IOException, InterruptedException {
		
		final SniffingDataCollector dataCollector = new SniffingDataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), dataCollector);

		final String given = "5A8 00 00 81 10 80 C0 02 BF\n\r";

		final MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("STM",given)
				.build();
		
		final SniffingPolicy sniffingPolicy = SniffingPolicy
				.builder()
				.enabled(true)
				.debugEnabled(false)
				.stNxx(STNxxExtensions
						.builder()
						.filter("384,0FF")
						.enabled(true)
						.build())
				.build();
		
		workflow.start(connection, sniffingPolicy);
		WorkflowFinalizer.finalizeAfter(workflow, 500);	
		

		final BlockingDeque<String> recordedQueries = (BlockingDeque<String>) connection.recordedQueries();
		
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATZ");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATE0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATL0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATH1");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATCAF0");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STFAC");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STFPA 384,0FF");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("ATSP6");
		Assertions.assertThat(recordedQueries.pop()).isEqualTo("STM");
		
		
		final List<SnifferMetric> findMetricsBy = dataCollector.findMetricsBy(workflow.getPidRegistry().findBy(Workflow.SNIFFING_PID_ID));
		Assertions.assertThat(findMetricsBy).isNotNull().isNotEmpty();
		Assertions.assertThat(findMetricsBy.get(0).getRaw().getMessage()).isNotNull().isEqualTo(given.substring(0, given.length() - 2));
	}
}
