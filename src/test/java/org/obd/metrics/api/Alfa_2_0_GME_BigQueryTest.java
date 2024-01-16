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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
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
import org.obd.metrics.connection.MockAdapterConnection;
import org.obd.metrics.transport.Connector;

public class Alfa_2_0_GME_BigQueryTest {

	@Test
	public void bigQueryTest() throws IOException, InterruptedException {

		// Create an instance of DataCollector that receives the OBD Metrics
		DataCollector collector = new DataCollector();

		// Getting the Workflow instance for mode 01
		Workflow workflow = SimpleWorkflowFactory.getWorkflow(new SimpleLifecycle(), collector, "giulia_2.0_gme.json",
				"mode01.json");

		final List<Long> pids = new ArrayList<>();
		CollectionUtils.addAll(pids,
				new Long[] { 7044l, 7043l, 7046l, 7001l, 7045l, 7006l, 7005l, 7042l, 7041l, 99l, 7055l, 12l, 7010l,
						7054l, 13l, 7057l, 14l, 7056l, 7059l, 7058l, 18l, 7051l, 7050l, 7053l, 5l, 7l, 7008l, 7007l,
						21l, 22l, 7066l, 23l, 7021l, 7065l, 7024l, 7068l, 7067l, 7026l, 27l, 7069l, 7028l, 7060l, 7062l,
						7061l, 7064l, 7063l, 7018l, 7035l, 7031l, 7030l, 7029l, 7002l, 7004l, 7003l, 7025l, 7047l,
						7027l, 7049l, 7040l, 7020l, 154l, 155l, 156l, 157l, 7019l, 10l, 7033l, 7032l, 7013l, 7034l, 15l,
						7015l, 7037l, 16l, 7014l, 7036l, 17l, 7017l, 7039l, 7016l, 7038l, 7052l, 6l, 8l, 7009l });

		final Query query = Query.builder().pids(pids).build();

		// Create an instance of mock connection with additional commands and replies
		final String longResponse = "02D0:6210020000191:5A03F6181F03F52:193703EC3A60003:003A530226182F4:000018410000185"
				+ ":92000018910000:92000018910000:92000018910000:92000018910000:92000018910000:92000018910000:92000018910000:92000018910000";

		Assertions.assertThat(longResponse.length()).isGreaterThan(Connector.BUFFER_SIZE);

		MockAdapterConnection connection = MockAdapterConnection.builder()
				.requestResponse("STPX H:18DA10F1, D:22 1002 195A 181F 1937 3A60 3A53 182F 1841 1892 1891 1894, R:6",
						longResponse)
				.requestResponse("STPX H:18DA10F1, D:22 1893 1959 1830 18AA 1000 1924 186C 1956 1834 1802 186E, R:6",
						"0280:6218930000191:5903F518AA00002:100000001924003:186C00001956034:FC1834000018025:0080186E0000")
				.requestResponse("STPX H:18DA10F1, D:22 186D 1812 186F 18AD 1831 18AE 1833 1832 130A 1942 1947, R:6",
						"02B0:62186D0000181:120000186F00002:18AD00001831003:0018AE000018334:000018320000135:0A1A1942001947")
				.build();

		// Enabling batch commands
		final Adjustments optional = Adjustments.builder().debugEnabled(true)
				.stNxx(STNxxExtensions.builder().enabled(true).build())
				.cachePolicy(CachePolicy.builder().storeResultCacheOnDisk(Boolean.FALSE)
						.resultCacheFilePath("./result_cache.json").resultCacheEnabled(Boolean.TRUE).build())
				.adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy.builder().enabled(Boolean.FALSE).checkInterval(5)
						.commandFrequency(6).build())
				.producerPolicy(ProducerPolicy.builder().priorityQueueEnabled(Boolean.TRUE).build())
				.batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build()).build();

		final Init init = Init.builder().delayAfterInit(1000)
				.header(Header.builder().mode("22").header("18DA10F1").build())
				.header(Header.builder().mode("01").header("18DB33F1").build()).protocol(Protocol.CAN_29)
				.sequence(DefaultCommandGroup.INIT).build();

		// Start background threads, that call the adapter,decode the raw data, and
		// populates OBD metrics
		workflow.start(connection, query, init, optional);

		// Starting the workflow completion job, it will end workflow after some period
		// of time (helper method)
		WorkflowFinalizer.finalizeAfter(workflow, 1500);

		// Ensure batch commands were sent out
		Assertions.assertThat(connection.recordedQueries())
				.contains("STPX H:18DA10F1, D:22 1002 195A 181F 1937 3A60 3A53 182F 1841 1892 1891 1894, R:6");

		Assertions.assertThat(connection.recordedQueries())
				.contains("STPX H:18DA10F1, D:22 1893 1959 1830 18AA 1000 1924 186C 1834 1802 186E 186D, R:6");

		Assertions.assertThat(connection.recordedQueries())
				.contains("STPX H:18DA10F1, D:22 1812 186F 18AD 1831 18AE 1833 1832 130A 1942 1947 1946, R:6");

		// Ensure we receive AT commands
		Assertions.assertThat(collector.findATResetCommand()).isNotNull();
	}
}
