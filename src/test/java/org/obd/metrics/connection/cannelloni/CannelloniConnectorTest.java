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
package org.obd.metrics.connection.cannelloni;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.command.obd.CannelloniMessage;
import org.obd.metrics.transport.Connector;
import org.obd.metrics.transport.TcpAdapterConnection;

import lombok.extern.slf4j.Slf4j;

///usr/local/bin/cannelloni -I vcan0 -C s -d c -p
// sudo systemctl status cannelloni-vcan0.service

//@Disabled
@Slf4j
public class CannelloniConnectorTest {

	public static void main(String[] args) {
		final String abc = "ABCDE";
		System.out.println(abc.getBytes().length);

	}

	@Disabled
	@Test
	public void transitBenchmarkTest() throws IOException {

		final TcpAdapterConnection tcpAdapterConnection = TcpAdapterConnection.of("127.0.0.1", 20000);

		final Connector connector = Connector.builder().connection(tcpAdapterConnection)
				.adjustments(Adjustments.builder().debugEnabled(true).build()).type(Connector.Type.CANNELLONI).build();

		connector.transmit(new CannelloniMessage());
		connector.receive().getMessage();

		long tt = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			final byte[] canId = new byte[] { 0x0, 0x0, (byte) i, 0x3 };
			final byte[] data = new byte[] { 0x2, (byte) i, 0x2 };
			final CannelloniMessage message = new CannelloniMessage(canId, data);
			connector.transmit(message);
		}
		tt = System.currentTimeMillis() - tt;
		log.info("transit time: {}", tt);
	}

	@Disabled
	@Test
	public void transitTest() throws IOException {

		final TcpAdapterConnection tcpAdapterConnection = TcpAdapterConnection.of("127.0.0.1", 20000);

		final Connector connector = Connector.builder().connection(tcpAdapterConnection)
				.adjustments(Adjustments.builder().debugEnabled(true).build()).type(Connector.Type.CANNELLONI).build();

		connector.transmit(new CannelloniMessage());
		connector.receive().getMessage();
		final CannelloniMessage message = new CannelloniMessage("7AB", "ABCDEF");
		connector.transmit(message);
	}
}
