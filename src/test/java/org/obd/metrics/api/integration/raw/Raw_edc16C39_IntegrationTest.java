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

public class Raw_edc16C39_IntegrationTest extends RawIntegrationRunner {
	
	@Test
	public void routineFanTest() throws IOException, InterruptedException, ExecutionException {
		
		final Pids pids = Pids.builder()
				.resource(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build();

		final CommandsBuffer buffer = CommandsBuffer.instance();
		
		buffer.addLast(new ATCommand("D"));
		buffer.addLast(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("L0")); // line feed off
		buffer.addLast(new ATCommand("H0")); 
		buffer.addLast(new ATCommand("E0"));
//		buffer.addLast(new ATCommand("PP2CSV01"));
//		buffer.addLast(new ATCommand("PP2CON"));
//		
//		buffer.addLast(new ATCommand("PP2DSV01"));
//		buffer.addLast(new ATCommand("PP2DON"));
		
//		buffer.addLast(new ATCommand("SPB"));
//		buffer.addLast(new ATCommand("S0"));
//		buffer.addLast(new ATCommand("AL"));
//		buffer.addLast(new ATCommand("CP18"));
		buffer.addLast(new ATCommand("DP"));
		
		
//		buffer.addLast(new ATCommand("CRA18DAF"));
		buffer.addLast(new ATCommand("SHDA10F1"));
		
		buffer.addLast(new ATCommand("AT1"));
		buffer.addLast(new ATCommand("ST99"));
        
		buffer.addLast(new ObdCommand("1003"));
		buffer.addLast(new ObdCommand("3E00"));
		
		buffer.addLast(new ObdCommand("222023"));
		buffer.addLast(new ObdCommand("221955"));
		buffer.addLast(new ObdCommand("222001"));
		
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

		runBtTest("AABBCC112233",pids, buffer, optional);
	}
}
