/** 
 * Copyright 2019-2023, Tomasz Żebrowski
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
package org.obd.metrics.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class TcpAdapterConnection implements AdapterConnection {

	private final InetSocketAddress inetSocketAddress;
	private final Socket socket = new Socket();

	private InputStream inputStream;
	private OutputStream outputStream;

	public static TcpAdapterConnection of(final String host, final int port) {
		return new TcpAdapterConnection(new InetSocketAddress(host, port));
	}

	@Override
	public void connect() throws IOException {
		log.info("Opening Tcp connection to '{}:{}'", inetSocketAddress.getHostName(), inetSocketAddress.getPort());
		socket.connect(inetSocketAddress);
		log.info("Tcp connection with '{}:{}' has been established", inetSocketAddress.getHostName(),
				inetSocketAddress.getPort());
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return inputStream = this.socket.getInputStream();
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return outputStream = this.socket.getOutputStream();
	}

	@Override
	public void close() {
		log.debug("Closing tcp connection.");
		try {
			if (inputStream != null) {
				inputStream.close();
				log.trace("Input stream has been closed");
			}
		} catch (final IOException e) {
		}

		try {
			if (outputStream != null) {
				outputStream.close();
				log.trace("Output stream has been closed");
			}
		} catch (final IOException e) {
		}

		try {
			if (socket != null) {
				socket.close();
				log.trace("Socket has been closed");
			}
		} catch (final IOException e) {
		}
	}

	@Override
	public void reconnect() throws IOException {
		close();
		try {
			Thread.sleep(500);
		} catch (final InterruptedException e) {
			log.debug("Failed to wait 500ms");
		}
		connect();
	}
}
