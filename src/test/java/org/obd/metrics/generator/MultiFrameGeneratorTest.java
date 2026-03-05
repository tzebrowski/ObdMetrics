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
package org.obd.metrics.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.codec.generator.Strategy;
import org.obd.metrics.connection.SmartMockAdapterConnection;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.PIDsRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;
import org.obd.metrics.transport.AdapterConnection;

public class MultiFrameGeneratorTest {

	private static final Adjustments optional = Adjustments
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
	        		.otherModesBatchSize(10)
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
	
	@Test
	public void stnTest() throws IOException, InterruptedException {
		
		final PIDsRegistry registry = PIDsRegistryFactory.get("alfa.json");
		final DataCollector collector = new DataCollector();
		final Workflow workflow = SimpleWorkflowFactory.getWorkflow(collector);
		final String pidList = "1000 1924 186B 1827 1828 1937 181F 180E 1867 186C 186D 186E 186F 1002 18AD 18AE 18C7 18AF 18C8 1910 1911";

		final Query query = Query.builder().pids(getPids(registry, pidList)).build();
		final Init init = Init.DEFAULT;
		
		final AdapterConnection connection = SmartMockAdapterConnection.get(registry, optional, query,
				init, Strategy.UniformRandom);
		
		workflow.start(connection, query, init, optional);

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		WorkflowFinalizer.finalizeAfter(workflow,5000);
	}


	private List<Long> getPids(final PIDsRegistry registry, final String input) {
		final String[] tokens = input.split("\\s+");
		final List<Long> pidList = new ArrayList<Long>();
		for (int i = 1; i < tokens.length; i++) {
			pidList.add(registry.findBy(tokens[i]).getId());
        }
		return pidList;
	}
}
