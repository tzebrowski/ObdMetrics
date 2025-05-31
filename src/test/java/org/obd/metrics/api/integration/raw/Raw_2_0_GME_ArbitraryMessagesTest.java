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

public class Raw_2_0_GME_ArbitraryMessagesTest extends RawIntegrationRunner {

	@Test
	public void start_stop_off() throws IOException, InterruptedException, ExecutionException {
		
		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("E0"));
		buffer.addLast(new ATCommand("SP6"));
		buffer.addLast(new ATCommand("CAF0"));

		buffer.addLast(new ATCommand("SH4B1"));
		buffer.addLast(new ObdCommand("04 00 00 10 A0 08 08 00"));		
		buffer.addLast(new ObdCommand("04 00 00 10 A0 08 08 00"));		

		buffer.addLast(new QuitCommand());

		executeCommandsBuffer(buffer);
	}

	@Test
	public void start_stop_on() throws IOException, InterruptedException, ExecutionException {
		
		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("E0"));
		buffer.addLast(new ATCommand("SP6"));
		buffer.addLast(new ATCommand("CAF0"));

		buffer.addLast(new ATCommand("SH4B1"));
		buffer.addLast(new ObdCommand("04 00 00 10 A0 08 00 00"));		
		buffer.addLast(new ObdCommand("04 00 00 10 A0 08 00 00"));		

		buffer.addLast(new QuitCommand());

		executeCommandsBuffer(buffer);
	}

	
	@Test
	public void sniffing() throws IOException, InterruptedException, ExecutionException {
		
		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("E0"));
		buffer.addLast(new ATCommand("L0"));
		buffer.addLast(new ATCommand("H1"));
		
		buffer.addLast(new ATCommand("SP6"));
		buffer.addLast(new ATCommand("CAF0"));

		buffer.addLast(new ATCommand("SH5AC"));
		buffer.addLast(new ObdCommand("STM"));
		
		buffer.addLast(new QuitCommand());
		executeCommandsBuffer(buffer, true);
	}
	
	protected void executeCommandsBuffer(final CommandsBuffer buffer)
			throws IOException, InterruptedException {
		executeCommandsBuffer(buffer,false);
	}

	protected void executeCommandsBuffer(final CommandsBuffer buffer, boolean sniffing)
			throws IOException, InterruptedException {
		final Adjustments optional = Adjustments.builder()
				.sniffingEnabled(sniffing)
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

		final Pids pids = Pids.builder()
				.resource(Thread.currentThread().getContextClassLoader().getResource("giulia_2.0_gme.json")).build();
		
			// AABBCC112233
		runBtTest("000D18000001", pids, buffer, optional);
//		runBtTest("AABBCC112233", pids, buffer, optional);
	}

}
