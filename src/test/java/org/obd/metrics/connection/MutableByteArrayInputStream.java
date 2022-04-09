package org.obd.metrics.connection;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

final class MutableByteArrayInputStream extends ByteArrayInputStream {
	private final long readTimeout;
	private final boolean simulateReadError;

	public MutableByteArrayInputStream(long readTimeout, boolean simulateReadError) {
		super("".getBytes());
		this.readTimeout = readTimeout;
		this.simulateReadError = simulateReadError;
	}

	@Override
	public synchronized int read() {
		if (simulateReadError) {
			throw new RuntimeException("Read exception");
		}

		int read = super.read();
		try {
			TimeUnit.MILLISECONDS.sleep(readTimeout);
		} catch (InterruptedException e) {
		}
		return read;
	}

	void update(String data) {
		this.buf = data.getBytes();
		this.pos = 0;
		this.count = buf.length;
	}
}