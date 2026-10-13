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
package org.obd.metrics.api;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.api.model.ObdMetric;
import org.obd.metrics.api.model.ReadinessStatus;
import org.obd.metrics.api.model.Reply;
import org.obd.metrics.api.model.ReplyObserver;
import org.obd.metrics.buffer.decoder.ConnectorResponseBuffer;
import org.obd.metrics.buffer.decoder.ConnectorResponseWrapper;
import org.obd.metrics.codec.CodecRegistry;
import org.obd.metrics.codec.formula.FormulaEvaluatorConfig;
import org.obd.metrics.command.obd.ObdCommand;
import org.obd.metrics.pid.PidDefinitionRegistry;
import org.obd.metrics.test.PIDsRegistryFactory;
import org.obd.metrics.transport.message.ConnectorResponseFactory;

public class ConnectorResponseDecoderTest {

	// Every query update queues UDS 10 01, a command without a PID. Its reply threw a NullPointerException in the
	// decoder, which ended the decoder thread: no reply was decoded again until the next connect.
	@Test
	public void replyToCommandWithoutPidDoesNotStopDecoding() throws Exception {
		final PidDefinitionRegistry registry = PIDsRegistryFactory.get("mode01.json");
		final List<Reply<?>> received = new CopyOnWriteArrayList<>();
		final ReplyObserver<Reply<?>> observer = new ReplyObserver<Reply<?>>() {
			@Override
			public void onNext(Reply<?> reply) {
				received.add(reply);
			}
		};

		final ConnectorResponseBuffer buffer = ConnectorResponseBuffer.instance();
		final ConnectorResponseDecoder decoder = new ConnectorResponseDecoder(buffer, Adjustments.DEFAULT, registry,
				CodecRegistry.builder().adjustments(Adjustments.DEFAULT)
						.formulaEvaluatorConfig(FormulaEvaluatorConfig.builder().build()).build(),
				EventsPublishlisher.builder().observer(observer).build());

		buffer.addLast(wrap(UDSConstants.UDS_DEFAULT_SESSION, "5001003201F4"));
		buffer.addLast(wrap(new ObdCommand(registry.findBy(21010L)), "410188077171"));

		final ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			executor.submit(decoder);
			final long deadline = System.currentTimeMillis() + 2000;
			while (received.isEmpty() && System.currentTimeMillis() < deadline) {
				Thread.sleep(10);
			}
		} finally {
			decoder.onStopping();
			executor.shutdownNow();
			executor.awaitTermination(1, TimeUnit.SECONDS);
		}

		Assertions.assertThat(received).hasSize(1);
		Assertions.assertThat(((ObdMetric) received.get(0)).getValue()).isInstanceOf(ReadinessStatus.class);
	}

	private static ConnectorResponseWrapper wrap(ObdCommand command, String message) {
		final ConnectorResponseWrapper wrapper = new ConnectorResponseWrapper();
		wrapper.setCommand(command);
		wrapper.setConnectorResponse(ConnectorResponseFactory.wrap(message.getBytes()));
		return wrapper;
	}
}
