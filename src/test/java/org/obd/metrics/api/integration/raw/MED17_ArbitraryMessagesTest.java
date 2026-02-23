 /**
 * Copyright 2019-2026, Tomasz Żebrowski
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
import org.obd.metrics.api.model.SniffingPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.group.DefaultCommandGroup;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.DelayCommand;
import org.obd.metrics.command.process.QuitCommand;

public class MED17_ArbitraryMessagesTest extends RawIntegrationRunner {
	
	@Test
	public void dynamicDiDTest() throws IOException, InterruptedException, ExecutionException {
		
		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.add(DefaultCommandGroup.INIT);

		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:1003, R:1"));		
		buffer.addLast(new DelayCommand(500));		
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:2711, R:1"));		
		buffer.addLast(new ObdCommand("STPX H:18DA10F1, D:2C03F200, R:1"));		
		
		buffer.addLast(new QuitCommand());
		
		executeCommandsBuffer(buffer);
	}

	
	protected void executeCommandsBuffer(final CommandsBuffer buffer)
			throws IOException, InterruptedException {
		executeCommandsBuffer(buffer,false);
	}

	protected void executeCommandsBuffer(final CommandsBuffer buffer, boolean sniffing)
			throws IOException, InterruptedException {
		
		final Adjustments optional = Adjustments.builder()
				.sniffing(SniffingPolicy.builder().enabled(Boolean.FALSE).build())
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
				.resource(Thread.currentThread().getContextClassLoader().getResource("alfa.json")).build();
		
		runBtTest("000D18000001", pids, buffer, optional);
	}

}
