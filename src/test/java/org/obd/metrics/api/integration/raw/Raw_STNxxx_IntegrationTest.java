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
package org.obd.metrics.api.integration.raw;

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

public class Raw_STNxxx_IntegrationTest extends RawIntegrationRunner {

	@Test
	public void case_01() throws IOException, InterruptedException, ExecutionException {

		final Pids pids = Pids.builder()
				.resource(Thread.currentThread().getContextClassLoader().getResource("giulia_2.0_gme.json")).build();
		
		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("D")); // default
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("L0")); // line feed off
		buffer.addLast(new ATCommand("H0")); 
		buffer.addLast(new ATCommand("E0"));
		buffer.addLast(new ATCommand("SP 6"));
//		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:22 1924 1003 1827 1828 181F 1002 1956 381A 181D 1862, R:2"));	// 1.75tbi	
		buffer.addLast(new ObdCommand("STPX H:7DF, D:01 0B 0C 0D 04 05 11"));		

		buffer.addLast(new QuitCommand());
		
		final Adjustments optional = Adjustments.builder()
				.adaptiveTimeoutPolicy(
						AdaptiveTimeoutPolicy
						.builder()
						.enabled(Boolean.TRUE)
						.checkInterval(5000)
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
