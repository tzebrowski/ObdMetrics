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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.test.DataCollector;
import org.obd.metrics.test.MockAdapterConnection;
import org.obd.metrics.test.SimpleLifecycle;
import org.obd.metrics.test.SimpleWorkflowFactory;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.test.WorkflowMonitor;

public class Odometer_Issue_Test {

	public static void main(String[] args) {
		String in = "0250:6219420028051:0000193703E6202:010A0E3718F0003:19353A130200134:18BA6C100400785:10033B";
		System.out.println(in.replace(":", ""));
		String a = "025062194200280510000193703E6202010A0E3718F000319353A13020013418BA6C10040078510033B";
		
	}
	
	@Test
	public void numberFormatExceptionTest() throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Getting the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), collector, "giulia_2.0_gme.json", "odometer_issue.json");

		final List<Long> pids = new ArrayList<>();
		CollectionUtils.addAll(pids,
				new Long[] { 7021l, 7076l, 7035l, 7002l, 7037l, 7047l, 7025l, 7003l, 7014l, 7036l, 7028l, 7016l, 17078l, 7005l, 7020l, 7019l, 7009l });

		final Query query = Query.builder().pids(pids).build();

//		TX: STPX H:18DA10F1, D:22 1942 2805 1937 130A 2001 18F0 1935 1302 18BA 1004
//		RX: 0250:6219420028051:0000193703E6132:0A1920010A0E373:18F00019353A134:02001318BA6C105:040078, processing time: 102ms
		
		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("STPX H:18DA10F1, D:22 1942 2805 1937 2001 18F0 1935 1302 18BA 1004 1003",
						"0250:6219420028051:0000193703E6202:010A0E3718F0003:19353A130200134:18BA6C100400785:10033B")
				.build();

		// Enabling batch commands
		final Adjustments optional = Adjustments.builder().debugEnabled(true)
				.stNxx(STNxxExtensions.builder()
						.enabled(true)
						.promoteAllGroupsEnabled(true)
						.promoteAllGroupsEnabled(true).build())
				.cachePolicy(CachePolicy.builder()
						.storeResultCacheOnDisk(Boolean.FALSE)
						.resultCacheEnabled(Boolean.FALSE).build())
				.adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy.builder()
						.enabled(Boolean.FALSE).checkInterval(5)
						.commandFrequency(6).build())
				.producerPolicy(ProducerPolicy.builder()
						.priorityQueueEnabled(Boolean.TRUE).build())
				.batchPolicy(BatchPolicy.builder()
						.responseLengthEnabled(false)
						.otherModesBatchSize(10)
						.enabled(Boolean.TRUE)
						.build()).build();

		final Init init = Init.builder().delayAfterInit(1000)
				.header(Header.builder().mode("22").header("18DA10F1").build())
				.header(Header.builder().mode("01").header("18DB33F1").build()).protocol(Protocol.CAN_29)
				.sequence(DefaultCommandGroup.INIT).build();

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);

		WorkflowMonitor.waitUntilRunning(workflow);
		Assertions.assertThat(workflow.isRunning()).isTrue();
		
		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 1500);

		// Ensure batch commands were sent out
		Assertions.assertThat(connection.recordedQueries())
				.contains("STPX H:18DA10F1, D:22 1942 2805 1937 2001 18F0 1935 1302 18BA 1004 1003");

		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
	}
}
