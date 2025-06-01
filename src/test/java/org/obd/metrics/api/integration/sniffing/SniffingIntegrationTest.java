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
package org.obd.metrics.api.integration.sniffing;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.api.Workflow;
import org.obd.metrics.api.integration.raw.RawIntegrationRunner;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.api.model.SniffingPolicy;
import org.obd.metrics.api.model.SniffingPolicy.STNxxExtensions;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;
import org.obd.metrics.connection.BluetoothConnection;
import org.obd.metrics.test.WorkflowFinalizer;
import org.obd.metrics.transport.AdapterConnection;

public class SniffingIntegrationTest extends RawIntegrationRunner {
	 
	@Test
	public void workflowSniffing() throws IOException, InterruptedException, ExecutionException {
		//000D18000001
		//AABBCC112233
		final AdapterConnection connection = BluetoothConnection.openConnection("000D18000001");
		
		final ReplyObserver<Reply<?>> printer = new ReplyObserver<Reply<?>>() {
		    @Override
		    public void onNext(Reply<?> t) {
		    	System.out.print(t.getRaw().getMessage());
		    }
		};
		
		final Workflow workflow = Workflow
		        .instance()
		        .observer(printer)
		        .initialize();
		
		final SniffingPolicy sniffingPolicy = SniffingPolicy
				.builder()
				.enabled(true)
				.debugEnabled(false)
				.stNxx(STNxxExtensions
						.builder()
						.enabled(true)
						.build())
				.build();
		
		workflow.startSniffing(connection, sniffingPolicy);
		WorkflowFinalizer.finalizeAfter(workflow, 10000);	
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

//		buffer.addLast(new ATCommand("SH7DF"));
		buffer.addLast(new ObdCommand("ATMA"));
		
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
				.sniffing(SniffingPolicy.builder().enabled(Boolean.TRUE).build())
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
		
		runBtTest("000D18000001", pids, buffer, optional);
//		runBtTest("AABBCC112233", pids, buffer, optional);
	}

}
