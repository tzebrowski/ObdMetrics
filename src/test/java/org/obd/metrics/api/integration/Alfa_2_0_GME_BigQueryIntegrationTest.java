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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.DataCollector;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.WorkflowFinalizer;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Init;
import org.obd.metrics.api.model.Init.Header;
import org.obd.metrics.api.model.Init.Protocol;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Query;
import org.obd.metrics.api.model.STNxxExtensions;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.TcpAdapterConnection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Alfa_2_0_GME_BigQueryIntegrationTest {
	
	@Test
	public void stnTest() throws IOException, InterruptedException, ExecutionException {
//		final AdapterConnection connection = BluetoothConnection.of("000D18000001"); 
		final AdapterConnection connection = TcpAdapterConnection.of("192.168.0.10", 35000);

		final DataCollector collector = new DataCollector(false);

		final Pids resources = Pids
		        .builder()
		        .resource(Thread.currentThread().getContextClassLoader().getResource("giulia_2.0_gme.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("extra.json"))
		        .resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json"))
				.build();
		
		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .pids(resources)
		        .observer(collector)
		        .initialize();
		
		final Long ids[] = new Long[] { 7044l, 7043l, 7046l, 7001l, 7045l, 7006l, 7005l, 7042l, 7041l, 99l, 7055l, 12l,
				7010l, 7054l, 13l, 7057l, 14l, 7056l, 7059l, 7058l, 18l, 7051l, 7050l, 7053l, 5l, 7l, 7008l, 7007l, 21l,
				22l, 7066l, 23l, 7021l, 7065l, 7024l, 7068l, 7067l, 7026l, 27l, 7069l, 7028l, 7060l, 7062l, 7061l,
				7064l, 7063l, 7018l, 7035l, 7031l, 7030l, 7029l, 7002l, 7004l, 7003l, 7025l, 7047l, 7027l, 7049l, 7040l,
				7020l, 154l, 155l, 156l, 157l, 7019l, 10l, 7033l, 7032l, 7013l, 7034l, 15l, 7015l, 7037l, 16l, 7014l,
				7036l, 17l, 7017l, 7039l, 7016l, 7038l, 7052l, 6l, 8l, 7009l };
		
		final List<Long> pids = new ArrayList<>();
		CollectionUtils.addAll(pids, ids);
		
		
		final Query query = Query.builder()
				.pids(pids) 
				.build();

		final Adjustments optional = Adjustments
		        .builder()
		        .stNxx(STNxxExtensions.builder().enabled(true).build())
		        .debugEnabled(true)
		        .vehicleMetadataReadingEnabled(Boolean.FALSE)
		        .vehicleCapabilitiesReadingEnabled(Boolean.FALSE)	
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(5000)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchPolicy(BatchPolicy.builder().enabled(true).build())
		        .cachePolicy(CachePolicy.builder().resultCacheEnabled(Boolean.FALSE).build())
		        .build();

		final Init init = Init.builder()
		        .delayAfterInit(1000)
		        .header(Header.builder().mode("22").header("18DA10F1").build())
				.header(Header.builder().mode("01").header("18DB33F1").build())
		        .protocol(Protocol.CAN_29)
		        .sequence(DefaultCommandGroup.INIT).build();
		
		workflow.start(connection, query, init, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 30000, () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", measuredPID, ratePerSec);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}

}
