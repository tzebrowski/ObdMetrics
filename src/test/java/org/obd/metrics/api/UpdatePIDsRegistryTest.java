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
import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.pid.Urls;
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

public class UpdatePIDsRegistryTest {

	@Test
	@DisplayName("Registry update. No workflow run")
	public void updateTest_case1() throws IOException, InterruptedException {

		// Getting the workflow - mode01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), new DataCollector(),"mode01.json");
		PidDefinitionRegistry pidRegistry = workflow.getPidRegistry();
		Assertions.assertThat(pidRegistry.findBy(12l)).isNotNull();

		Assertions.assertThat(pidRegistry.findBy(7001L)).isNull();
		// Updating the registry with giulia_2.0_gme
		workflow.updatePidRegistry(Pids.builder().resource(Urls.resourceToUrl("giulia_2.0_gme.json")).build());
		pidRegistry = workflow.getPidRegistry();
		 
		Assertions.assertThat(pidRegistry.findBy(7001L)).isNotNull();
		Assertions.assertThat(pidRegistry.findBy(12l)).isNull();
	}
	
	
	@Test
	@DisplayName("Workflow run. Registry update happens. Metrics are emited")
	public void updateTest_case2() throws IOException, InterruptedException {

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
		
		final DataCollector dataCollector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), dataCollector,"mode01.json");
		final PIDsRegistry registry = PIDsRegistryFactory.get("giulia_2.0_gme.json");
		final String pidList = "1935 1302 1937 181F 1937 1924";
		final Query query = QuerySupport.build(registry, pidList);
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
		
		workflow.updatePidRegistry(Pids.builder().resource(Urls.resourceToUrl("giulia_2.0_gme.json")).build());
		workflow.start(connection, query, init, adjustments);
		
		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		WorkflowFinalizer.finalizeAfter(workflow, 800);
		
		
		Arrays.asList(pidList.split("\\s+")).forEach( p -> {
			Assertions.assertThat(dataCollector.findMetricsBy(registry.findBy(p)))
			.as("Metrics should not be empty for PID: %s", p)
			.isNotEmpty();
		});
	}

	
	@Test
	@DisplayName("Workflow run. No registry update. Metrics are not emited.")
	public void updateTest_case3() throws IOException, InterruptedException {
		
		final Adjustments stn = Adjustments
		        .builder()
		        .debugEnabled(true)
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
		        		.otherModesBatchSize(5)
		        		.enabled(Boolean.TRUE)
		        		.calculateResponseFrames(false)
		        		.build())
		        .stNxx(STNxxExtensions
	                        .builder()
	                        .enabled(true)
	                        .stripWhitespaces(false)
	                        .promoteSlowGroupsEnabled(false)
	                        .promoteAllGroupsEnabled(false)
	                        .build()
	                )
		        
		        .build();
		
		final PIDsRegistry registry = PIDsRegistryFactory.get("alfa.json");
		DataCollector dataCollector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), dataCollector, "giulia_2.0_gme.json");
		final String pidList = "1000 1924 186B 1827 1828 1937 181F 180E 1867 186C 186D 186E 186F 1002 18AD 18AE 18C7 18AF 18C8 1910 1911";
		
		final Query query = QuerySupport.build(registry, pidList);
		
		final Init init = Init.DEFAULT;
		final AdapterConnection connection = SmartMockConnectionFactory
				.smartBuilder()
				.init(init)
				.registry(registry)
				.query(query)
				.optional(stn)
				.strategy(Strategy.UniformRandom)
				.jsEngineName("JavaScript")
				.responseCount(10)
				.build();
		
		workflow.start(connection, query, init, stn);

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		WorkflowFinalizer.finalizeAfter(workflow, 800);
		
		Arrays.asList(pidList.split("\\s+")).forEach( p -> {
			Assertions.assertThat(dataCollector.findMetricsBy(registry.findBy(p))).isEmpty();
		});
		
	}
}
