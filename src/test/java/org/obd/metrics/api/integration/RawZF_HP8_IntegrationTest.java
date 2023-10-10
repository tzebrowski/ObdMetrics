/** 
 * Copyright 2019-2023, Tomasz Żebrowski
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

import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;

public class RawZF_HP8_IntegrationTest extends RawIntegrationRunner {

	@Test
	public void case_01() throws IOException, InterruptedException, ExecutionException {
	
		final Pids pids = Pids.builder()
				.resource(Thread.currentThread().getContextClassLoader().getResource("giulia_2.0_gme.json")).build();
		
		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("L0")); // line feed off
		buffer.addLast(new ATCommand("H0")); 
		buffer.addLast(new ATCommand("E0"));
		buffer.addLast(new ATCommand("SPB"));
		buffer.addLast(new ATCommand("S0"));
		buffer.addLast(new ATCommand("AL"));
		buffer.addLast(new ATCommand("CP18"));
//		buffer.addLast(new ATCommand("CRA18DAF118"));
//		buffer.addLast(new ATCommand("SHDA18F1"));
		buffer.addLast(new ATCommand("AT1"));
		buffer.addLast(new ATCommand("ST99"));

		buffer.addLast(new ObdCommand("222023"));
//		buffer.addLast(new ObdCommand("222024"));
//		buffer.addLast(new ObdCommand("22F1A5"));
//		buffer.addLast(new ObdCommand("22F190"));
//		buffer.addLast(new ObdCommand("22F18C"));
//		buffer.addLast(new ObdCommand("22F187"));
//		buffer.addLast(new ObdCommand("22F192"));
//		buffer.addLast(new ObdCommand("22F193"));
//		buffer.addLast(new ObdCommand("22F194"));
//		buffer.addLast(new ObdCommand("22F195"));
//		buffer.addLast(new ObdCommand("22F196"));
//		buffer.addLast(new ObdCommand("22F191"));
//
//		buffer.addLast(new ObdCommand("22 051A"));
//		buffer.addLast(new ObdCommand("22 1018"));
//		buffer.addLast(new ObdCommand("22 04FE"));

//		buffer.addLast(new ObdCommand("22 04FE 051A 04FE"));
		
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F, R:5"));		
		buffer.addLast(new ObdCommand("STPX H:18DA18F1, D:22 04FE, R:1"));
		
		buffer.addLast(new ObdCommand("STPX H:18DB33F1, D:01 0B 0C 11, R:2"));
		buffer.addLast(new ObdCommand("STPX H:18DA18F1, D:22 1018, R:1"));
		
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 130A 195A 1937 181F 1924 1000 182F, R:5"));		
		buffer.addLast(new ObdCommand("STPX H:18DA18F1, D:22 04FE, R:1"));
		
//		buffer.addLast(new ObdCommand("STPX H:18DA18F1, D:22 04FE 1018 051A, R:2"));
//		buffer.addLast(new ObdCommand("STPX H:18DA18F1, D:22 04FE 1018 051A, R:2"));		

		buffer.addLast(new QuitCommand());
		
		final Adjustments optional = Adjustments.builder()
				.debugEnabled(Boolean.TRUE)
				.adaptiveTimeoutPolicy(
						AdaptiveTimeoutPolicy
						.builder()
						.enabled(Boolean.TRUE)
						.checkInterval(10)
						.commandFrequency(6)
						.build())
				.producerPolicy(ProducerPolicy
						.builder()
						.priorityQueueEnabled(Boolean.TRUE).build())
				.cachePolicy(CachePolicy
						.builder()
						.resultCacheEnabled(Boolean.FALSE).build())
				.batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build())
				.build();
		
		runBtTest(pids, buffer, optional);
	}
	
}
