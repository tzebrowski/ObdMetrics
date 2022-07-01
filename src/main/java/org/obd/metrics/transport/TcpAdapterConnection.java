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
