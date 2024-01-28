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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.command.obd.CannelloniMessage;
import org.obd.metrics.transport.CanUtils;
import org.obd.metrics.transport.Connector;
import org.obd.metrics.transport.TcpAdapterConnection;

import lombok.extern.slf4j.Slf4j;

///usr/local/bin/cannelloni -I vcan0 -C s -d c -p
// sudo systemctl stop cannelloni-vcan0.service

@Disabled
@Slf4j
public class CannelloniConnectorTest {

	@Disabled
	@Test
	public void idLogPareser()  throws FileNotFoundException {
		final Scanner scanner = new Scanner(new File("src/main/resources/giulietta_cannolleni_log.txt"));

		while (scanner.hasNextLine()) {
			String nextLine = scanner.nextLine();
			int start = nextLine.indexOf("[");
			int end = nextLine.indexOf("]");
			if (end > 0 && start > 0) {
				int value = Integer.parseInt(nextLine.substring(start + 1,end));
				log.info(" {} = {}",value, CanUtils.intToHex(value));
			}
		}
		scanner.close();
	}

	@Test
	public void idDecodingTest() {
		final String givenHex = "18DA10F1";
		final int givenInt = 416944369;
		Assertions.assertThat(CanUtils.hexToInt(givenHex)).isEqualTo(givenInt);
		Assertions.assertThat(CanUtils.intToHex(givenInt)).isEqualTo(givenHex);
	}

	@Disabled
	@Test
	public void transitBenchmarkTest() throws IOException {

		final TcpAdapterConnection tcpAdapterConnection = TcpAdapterConnection.of("192.168.148.135", 20000);

		final Connector connector = Connector.builder().connection(tcpAdapterConnection)
				.adjustments(Adjustments.builder().debugEnabled(true).build()).type(Connector.Type.CANNELLONI).build();

		connector.transmit(CannelloniMessage.hello());
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


	@Test
	public void transitTest() throws IOException, InterruptedException {

		final TcpAdapterConnection tcpAdapterConnection = TcpAdapterConnection.of("127.0.0.1", 20000);

		final Connector connector = Connector.builder()
				.connection(tcpAdapterConnection)
				.adjustments(Adjustments.builder().debugEnabled(true).build())
				.type(Connector.Type.CANNELLONI).build();

		connector.transmit(CannelloniMessage.hello());
		connector.receive().getMessage();
		final CannelloniMessage message = new CannelloniMessage("7df", "12345");
		connector.transmit(message);
	}
	
	
	@Disabled
	@Test
	public void readTest() throws IOException, InterruptedException {

		final TcpAdapterConnection tcpAdapterConnection = TcpAdapterConnection.of("127.0.0.1", 20000);

		final Connector connector = Connector.builder()
				.connection(tcpAdapterConnection)
				.adjustments(Adjustments.builder().debugEnabled(true).build())
				.type(Connector.Type.CANNELLONI).build();

		connector.transmit(CannelloniMessage.hello());
		connector.receive().getMessage();

		while (true) {
			Thread.sleep(1);
			connector.receive().getMessage();
		}
	}
	
}
