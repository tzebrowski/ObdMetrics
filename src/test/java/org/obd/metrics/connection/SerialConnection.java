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
package org.obd.metrics.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fazecast.jSerialComm.SerialPort;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class SerialConnection implements org.obd.metrics.transport.AdapterConnection {

	private final String portName;

	private OutputStream out;
	private InputStream in;

	public static org.obd.metrics.transport.AdapterConnection of(String portName) throws IOException {
		return new SerialConnection(portName);
	}

	@Override
	public void connect() throws IOException {
		final SerialPort obdPort = SerialPort.getCommPort(portName);

		obdPort.setBaudRate(38400);
		obdPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 2000, 0);

		if (!obdPort.openPort()) {
			return;
		}

		out = obdPort.getOutputStream();
		in = obdPort.getInputStream();
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return in;
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return out;
	}

	@Override
	public void close() throws IOException {
		if (in != null) {
			in.close();
		}

		if (out != null) {
			out.close();
		}
	}

	@Override
	public void reconnect() throws IOException {
	}
}
