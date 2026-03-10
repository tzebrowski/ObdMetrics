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
package org.obd.metrics.api.integration.sniffing;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.obd.metrics.api.integration.raw.BleRawIntegrationRunner;
import org.obd.metrics.api.model.AdaptiveTimeoutPolicy;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.BatchPolicy;
import org.obd.metrics.api.model.CachePolicy;
import org.obd.metrics.api.model.Pids;
import org.obd.metrics.api.model.ProducerPolicy;
import org.obd.metrics.api.model.SniffingPolicy;
import org.obd.metrics.buffer.CommandsBuffer;
import org.obd.metrics.command.ATCommand;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.command.process.QuitCommand;

public class SniffingIntegrationTest extends BleRawIntegrationRunner {

	private static byte calculateCRC(int[] data) {
		int arraySize = data.length;
		int crc = 0xFF;

		if (arraySize > 1) {
			for (int i = 0; i < arraySize - 1; i++) {
				crc ^= (data[i] & 0xFF); // Ensure unsigned behavior
				for (int j = 0; j < 8; ++j) {
					if ((crc & 0x80) != 0) {
						crc = ((crc << 1) ^ 0x1D) & 0xFF;
					} else {
						crc = (crc << 1) & 0xFF;
					}
				}
			}
			return (byte) (crc ^ 0xFF);
		}
		return 0;
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

		buffer.addLast(new ObdCommand("STFAC"));

		buffer.addLast(new ObdCommand("STFPA 1EF,FFF"));
//		buffer.addLast(new ObdCommand("STFPA 384,FFF"));
//		buffer.addLast(new ObdCommand("STFPA 5AC,FF0"));
//		

		buffer.addLast(new ObdCommand("STM"));
		buffer.addLast(new ObdCommand("STM"));
		buffer.addLast(new ObdCommand("STM"));
//		545 78 1A 02 04 00 13 40 00 

		buffer.addLast(new QuitCommand());
		executeCommandsBuffer(buffer, true);
	}

	@Test
	public void t1() throws IOException, InterruptedException, ExecutionException {

		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("E0"));
		buffer.addLast(new ATCommand("SP6"));
		buffer.addLast(new ATCommand("CAF0"));

		buffer.addLast(new ATCommand("SH192"));
		buffer.addLast(new ObdCommand("42 00 00 01 25"));

		buffer.addLast(new QuitCommand());

		executeCommandsBuffer(buffer);
	}

	@Test
	public void t2() throws IOException, InterruptedException, ExecutionException {

		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("E0"));
		buffer.addLast(new ATCommand("SP6"));
		buffer.addLast(new ATCommand("CAF0"));

		buffer.addLast(new ATCommand("SH1EF"));
//		buffer.addLast(new ObdCommand("80 00 00 00 00 00 00 00"));
		buffer.addLast(new ObdCommand("82 00 00 00 00 00 06 5F"));

//		buffer.addLast(new ObdCommand("02 00 00 00 00 00 00 00"));

		buffer.addLast(new QuitCommand());

		executeCommandsBuffer(buffer);
	}

	@Test
	public void chime() throws IOException, InterruptedException, ExecutionException {

		final CommandsBuffer buffer = CommandsBuffer.instance();
		buffer.addFirst(new ATCommand("Z")); // reset
		buffer.addLast(new ATCommand("E0"));
		buffer.addLast(new ATCommand("SP6"));
		buffer.addLast(new ATCommand("CAF0"));

		buffer.addLast(new ATCommand("SH545"));

		buffer.addLast(new ObdCommand("78 1A 02 04 00 13 40 00"));
		buffer.addLast(new ObdCommand("78 1A 02 04 00 13 40 00"));

//		buffer.addLast(new ObdCommand("20 00 00 00 00 00 00 65"));

		buffer.addLast(new QuitCommand());

		executeCommandsBuffer(buffer);
	}

	protected void executeCommandsBuffer(final CommandsBuffer buffer) throws IOException, InterruptedException {
		executeCommandsBuffer(buffer, false);
	}

	protected void executeCommandsBuffer(final CommandsBuffer buffer, boolean sniffing)
			throws IOException, InterruptedException {
		final Adjustments optional = Adjustments.builder()
				.sniffing(SniffingPolicy.builder().enabled(Boolean.TRUE).debugEnabled(Boolean.TRUE).build())
				.adaptiveTimeoutPolicy(AdaptiveTimeoutPolicy.builder().enabled(Boolean.TRUE).checkInterval(10)
						.commandFrequency(6).build())
				.producerPolicy(ProducerPolicy.builder().priorityQueueEnabled(Boolean.TRUE).build())
				.cachePolicy(CachePolicy.builder().resultCacheEnabled(Boolean.FALSE).build())

				.batchPolicy(BatchPolicy.builder().enabled(Boolean.TRUE).build()).build();

		final Pids pids = Pids.builder()
				.resource(Thread.currentThread().getContextClassLoader().getResource("giulia_2.0_gme.json")).build();

		runBtTest("000D18000001", pids, buffer, optional);
//		runBtTest("AABBCC112233", pids, buffer, optional);
	}

}
