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
package org.obd.metrics.connection.mock;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.QuerySupport;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.PIDsRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.mock.SmartMockConnectionFactory;
import org.obd.metrics.transport.mock.strategy.Strategy;

public class SmartMockAdapterConnectionTest {



	@Test
	public void defultTest() throws IOException, InterruptedException {
		
		final Adjustments adjustments = Adjustments
		        .builder()
		        .debugEnabled(false)
		        .cachePolicy(
		        		CachePolicy.builder()
		        		.storeResultCacheOnDisk(Boolean.FALSE)
		        		.resultCacheEnabled(Boolean.TRUE).build())
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.FALSE)
		                .build())
		        .batchPolicy(BatchPolicy
		        		.builder()
		        		.otherModesBatchSize(10)
		        		.enabled(Boolean.TRUE)
		        		.calculateResponseFrames(false)
		        		.build())
		        .stNxx(STNxxExtensions
	                        .builder()
	                        .enabled(false)
	                        .build())
		        .build();
		
		final PIDsRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final DataCollector dataCollector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), dataCollector, "giulia_2.0_gme.json");
		final Query query = QuerySupport.build(registry, "195A 1935 1302 1937 181F 1937 1924");
		final Init init = Init.DEFAULT;
		final AdapterConnection connection = SmartMockConnectionFactory
				.smartBuilder()
				.init(init)
				.registry(registry)
				.query(query)
				.optional(adjustments)
				.strategy(Strategy.UniformRandom)
				.jsEngineName("JavaScript")
				.responseCount(10)
				.build();
		
		workflow.start(connection, query, init, adjustments);

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		WorkflowFinalizer.finalizeAfter(workflow,2000);
		
		
		final List<ObdMetric> metricsBy = dataCollector.findMetricsBy(registry.findBy("1935"));
		Assertions.assertThat(metricsBy).isNotNull().hasSizeGreaterThan(0);
	}
}
