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
package org.obd.metrics.api.integration;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFinalizer;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.TcpAdapterConnection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntegrationTest {
	// [01, 03, 04, 05, 06, 07, 0b, 0c, 0d, 0e, 0f, 10, 11, 13, 15, 1c],
	// raw=4100be3fa811]

	@Test
	public void tcpConnection() throws IOException, InterruptedException, ExecutionException {

		final AdapterConnection connection = TcpAdapterConnection.of("192.168.0.10", 35000);
//		final AdapterConnection connection = TcpAdapterConnection.of("127.0.0.1", 5555);

		final DataCollector collector = new DataCollector();

		final Pids pids = Pids.builder()
				.resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json")).build();

		int commandFrequency = 10;
		final Workflow workflow = Workflow.instance().pids(pids).observer(collector).initialize();

		final Query query = Query.builder()
				.pid(5l)
				.pid(6l)
				.pid(12l)
				.pid(13l)
				.pid(14l)
				.pid(15l)
				.pid(22l)
				.pid(16l)
				.pid(18l)
				.pid(21l).build();

		final Adjustments optional = Adjustments.builder()
				.vehicleMetadataReadingEnabled(Boolean.TRUE)
				.vehicleCapabilitiesReadingEnabled(Boolean.TRUE)
				.adaptiveTimeoutPolicy(
						AdaptiveTimeoutPolicy
						.builder()
						.enabled(Boolean.TRUE)
						.checkInterval(5000)
						.commandFrequency(commandFrequency)
						.build())
				.producerPolicy(ProducerPolicy
						.builder()
						.priorityQueueEnabled(Boolean.TRUE).build())
				.cachePolicy(CachePolicy
						.builder()
						.resultCacheEnabled(Boolean.FALSE).build())
				.build();
		
		final Init init = Init.builder()
				.delayAfterInit(0)
		        .header(Header.builder().service("01").value("DB33F1").build())
		        .protocol(Protocol.CAN_29)
				.sequence(DefaultCommandGroup.INIT)
				.build();

		workflow.start(connection, query, init, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 25000, () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID.getPid(), ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}
}
