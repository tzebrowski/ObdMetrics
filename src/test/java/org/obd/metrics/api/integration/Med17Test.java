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
import org.obd.metrics.PIDsRegistry;
import org.obd.metrics.PIDsRegistryFactory;
import org.obd.metrics.api.Workflow;
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
import org.obd.metrics.connection.BluetoothConnection;
import org.obd.metrics.diagnostic.RateType;
import org.obd.metrics.pid.PidDefinition;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.utils.DataCollector;
import org.obd.metrics.test.utils.WorkflowFinalizer;
import org.obd.metrics.transport.AdapterConnection;
import org.obd.metrics.transport.TcpAdapterConnection;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Med17Test {
	// [01, 03, 04, 05, 06, 07, 0b, 0c, 0d, 0e, 0f, 10, 11, 13, 15, 1c],
	// raw=4100be3fa811]
	
	@Test
	public void stnTest() throws IOException, InterruptedException, ExecutionException {
		final Logger logger = (Logger) LoggerFactory.getLogger("org.obd.metrics.transport.DefaultConnector");
		logger.setLevel(Level.TRACE);
		
		final AdapterConnection connection = TcpAdapterConnection.of("192.168.0.10", 35000);

		final DataCollector collector = new DataCollector();

		int commandFrequency = 6;
		final Workflow workflow = Workflow
		        .instance()
		        .pids(Pids.DEFAULT)
		        .observer(collector)
		        .initialize();
		
		final PIDsRegistry registry = PIDsRegistryFactory.get("mode01.json");
		final Query query = Query.builder()
		        .pid(registry.findBy("15").getId())
		        .pid(registry.findBy("0D").getId())
		        .pid(registry.findBy("0E").getId())
		        .pid(registry.findBy("0B").getId())
		        .pid(registry.findBy("0C").getId()) 
		        .pid(registry.findBy("04").getId()) 
		        .pid(registry.findBy("0F").getId())
		        .pid(registry.findBy("05").getId())
		        .build();

		final Adjustments optional = Adjustments
		        .builder()
		        .stNxx(STNxxExtensions.builder().enabled(Boolean.TRUE).build())
		        .vehicleCapabilitiesReadingEnabled(Boolean.TRUE)
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.TRUE)
		                .checkInterval(1)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .cachePolicy(CachePolicy.builder().resultCacheEnabled(false).build())
		        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
		        .build();

		 final Init init = Init.builder()
			.header(Header.builder().header("7DF").mode("01").build())   
            .delayAfterInit(0)
	        .protocol(Protocol.CAN_11)
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
	
	@Test
	public void btConnection() throws IOException, InterruptedException, ExecutionException {
		final AdapterConnection connection = BluetoothConnection.openConnection();
		final DataCollector collector = new DataCollector();

		int commandFrequency = 15;
		final Workflow workflow = Workflow
		        .instance()
		        .pids(Pids.DEFAULT)
		        .observer(collector)
		        .initialize();

		final PIDsRegistry registry = PIDsRegistryFactory.get("mode01.json");

		final Query query = Query.builder()
		        .pid(registry.findBy("15").getId()) 
		        .pid(registry.findBy("0B").getId())
		        .pid(registry.findBy("0C").getId()) 
		        .pid(registry.findBy("04").getId()) 
		        .pid(registry.findBy("11").getId())
//		        .pid(registry.findBy("0E").getId())
//		        .pid(registry.findBy("0F").getId())
		        .pid(registry.findBy("05").getId())
		        
		        .build();

		final Adjustments optional = Adjustments
		        .builder()
		        .adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy
		                .builder()
		                .enabled(Boolean.FALSE)
		                .checkInterval(5000)
		                .commandFrequency(commandFrequency)
		                .build())
		        .producerPolicy(ProducerPolicy.builder()
		                .priorityQueueEnabled(Boolean.TRUE)
		                .build())
		        .batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
		        .build();

		workflow.start(connection, query, Init.DEFAULT, optional);

		WorkflowFinalizer.finalizeAfter(workflow, 15000, () -> false);

		final PidDefinitionRegistry rpm = workflow.getPidRegistry();

		PidDefinition measuredPID = rpm.findBy(13l);
		double ratePerSec = workflow.getDiagnostics().rate().findBy(RateType.MEAN, measuredPID).get().getValue();

		log.info("Rate:{}  ->  {}", ratePerSec, measuredPID);

		Assertions.assertThat(ratePerSec).isGreaterThanOrEqualTo(commandFrequency);
	}
}
