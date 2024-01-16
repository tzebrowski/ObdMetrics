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

public class Raw_2_0_GME_IntegrationTest extends RawIntegrationRunner {
	
	@Test
	public void mode_01_tests() throws IOException, InterruptedException, ExecutionException {
		
		final Pids pids = Pids.builder()
				.resource(Thread.currentThread().getContextClassLoader().getResource("mode01.json")).build();

		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("L0")); // line feed off
		buffer.addLast(new ATCommand("H0")); 
		buffer.addLast(new ATCommand("E0"));
		buffer.addLast(new ATCommand("SPB"));
//		buffer.addLast(new ATCommand("S0"));
//		buffer.addLast(new ATCommand("AL"));
//		buffer.addLast(new ATCommand("CP18"));
//		buffer.addLast(new ATCommand("AT1"));
//		buffer.addLast(new ATCommand("ST99"));

		for (int i=0; i<50; i++) {
			buffer.addLast(new ObdCommand("STPX H:18DB33F1, D:01 15 0C 04 06 11 0E, R:4"));		
		}
		
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
	
	
	@Test
	public void mode_22_tests() throws IOException, InterruptedException, ExecutionException {
		
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

		
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004, R:6"));		
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004, R:7"));
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004, R:9"));
		
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA 1004"));

		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA, R:5"));
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA, R:6"));
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA, R:7"));
			
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 181F 1937 130A 1924 1956 1935 1302 1837 3A58 18BA"));

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
