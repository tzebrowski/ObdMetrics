package org.obd.metrics.connection.cannelloni;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.obd.metrics.api.model.Adjustments;
import org.obd.metrics.command.obd.CannelloniCommand;
import org.obd.metrics.transport.Connector;
import org.obd.metrics.transport.TcpAdapterConnection;

import lombok.extern.slf4j.Slf4j;

///usr/local/bin/cannelloni -I vcan0 -C s -d c -p
// sudo systemctl status cannelloni-vcan0.service
		
@Disabled
@Slf4j
public class CannelloniConnectorTest {
	
	@Test
	public void transmitTest() throws IOException {

		final TcpAdapterConnection tcpAdapterConnection = TcpAdapterConnection.of("127.0.0.1", 20000);

		final Connector connector = Connector
				.builder()
				.connection(tcpAdapterConnection)
				.adjustments(Adjustments.builder().debugEnabled(true).build()).type(Connector.Type.CANNELLONI).build();

		connector.transmit(new CannelloniCommand());
		connector.receive().getMessage();
		
		long tt = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			final byte[] canId = new byte[] { 0x0, 0x0, (byte) i, 0x3};
			final byte[] data = new byte[] { 0x2, (byte) i, 0x2 };
			final CannelloniCommand message = new CannelloniCommand(canId, data);
			connector.transmit(message);
		}
		tt = System.currentTimeMillis() - tt;
		log.info("transit time: {}", tt);
	}

}
