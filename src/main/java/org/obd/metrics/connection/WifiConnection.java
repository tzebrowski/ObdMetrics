package org.obd.metrics.connection;

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
public final class WifiConnection implements AdapterConnection {

	private final String host;
	private final int port;
	private final Socket socket = new Socket();

	private InputStream inputStream;
	private OutputStream outputStream;

	public static WifiConnection openConnection(String host, int port) throws IOException {
		return new WifiConnection(host, port);
	}

	@Override
	public void connect() throws IOException {
		log.info("Connecting to '{}:{}'", host, port);
		socket.connect(new InetSocketAddress(host, port));
		log.info("Connection to '{}:{}' has been established", host, port);
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
	public void close() throws IOException {
		inputStream.close();
		outputStream.close();
		socket.close();
	}

	@Override
	public void reconnect() throws IOException {
		close();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connect();
	}
}
